import { useCallback, useEffect, useRef, useState } from "react";
import { WS_BASE_URL } from "@/constants/endpoint";
import { tokenStore } from "@/utils/tokenStore";
import type {
  InterimEntry,
  OutgoingMessageType,
  RoomParticipant,
  TranscriptEntry,
} from "../types";

interface UseMeetingSignalingOptions {
  meetingId: number | null;
  displayName: string;
}

// WebSocket frame 형태
interface SignalingFrame {
  type: string;
  meeting_id?: number;
  participants?: Array<{
    id?: string | number;
    member_id?: string | number;
    name?: string;
    self?: boolean;
  }>;
  from_member_id?: number;
  from_name?: string;
  member_id?: number;
  name?: string;
  text?: string;
  timestamp?: string;
}

const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 2000;
const INTENTIONAL_CLOSE_REASONS = new Set([
  "unmount",
  "reconnect",
  "page hide",
]);

/**
 * 회의 WebSocket signaling 채널 관리.
 * - 참가자 roster / join / leave
 * - 실시간 transcript (interim / final)
 * - 메세지 송신 (chat / audio_text / speech)
 * - 비정상 종료 시 자동 재시도
 */
export const useMeetingSignaling = ({
  meetingId,
  displayName,
}: UseMeetingSignalingOptions) => {
  const [participants, setParticipants] = useState<RoomParticipant[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [retryAttempt, setRetryAttempt] = useState(0);
  const [isMeetingEnded, setIsMeetingEnded] = useState(false);
  const [transcripts, setTranscripts] = useState<TranscriptEntry[]>([]);
  const [interimByMember, setInterimByMember] = useState<
    Record<string, InterimEntry>
  >({});
  const [, setRetrySignal] = useState(0);

  const wsRef = useRef<WebSocket | null>(null);

  const sendMessage = useCallback((type: OutgoingMessageType, text: string) => {
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) return false;
    ws.send(JSON.stringify({ type, text }));
    return true;
  }, []);

  const manualRetry = useCallback(() => {
    setErrorMessage(null);
    setRetryAttempt(0);
    setIsConnected(false);
    setIsMeetingEnded(false);
    setRetrySignal((n) => n + 1);
  }, []);

  useEffect(() => {
    if (meetingId == null) return;
    if (!displayName) return;

    const accessToken = tokenStore.getToken();
    if (!accessToken) {
      setErrorMessage("로그인이 필요합니다.");
      return;
    }

    let cancelled = false;
    let attempt = 0;
    let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

    // 직전 effect 의 ws 가 아직 살아있다면 먼저 닫아 중복 연결 방지
    if (
      wsRef.current &&
      wsRef.current.readyState !== WebSocket.CLOSED &&
      wsRef.current.readyState !== WebSocket.CLOSING
    ) {
      try {
        wsRef.current.close(1000, "reconnect");
      } catch {
        /* noop */
      }
    }

    const wsUrl =
      `${WS_BASE_URL}/ws/meetings` +
      `?meetingId=${meetingId}` +
      `&accessToken=${encodeURIComponent(accessToken)}` +
      `&name=${encodeURIComponent(displayName)}`;

    const connect = () => {
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        if (cancelled) return;
        setIsConnected(true);
        setErrorMessage(null);
        setRetryAttempt(0);
        attempt = 0;
      };

      ws.onmessage = (ev) => {
        try {
          const msg = JSON.parse(ev.data) as SignalingFrame;
          switch (msg.type) {
            case "connected":
            case "roster": {
              if (Array.isArray(msg.participants)) {
                setParticipants(
                  msg.participants.map((p, idx) => ({
                    id: String(p.member_id ?? p.id ?? idx),
                    name: p.name ?? `참가자${idx + 1}`,
                    isSelf: p.self,
                  })),
                );
              }
              break;
            }
            case "participant_joined": {
              if (msg.member_id != null) {
                setParticipants((prev) => {
                  const id = String(msg.member_id);
                  if (prev.some((p) => p.id === id)) return prev;
                  return [
                    ...prev,
                    { id, name: msg.name ?? `참가자${prev.length + 1}` },
                  ];
                });
              }
              break;
            }
            case "participant_left": {
              if (msg.member_id != null) {
                const leftId = String(msg.member_id);
                setParticipants((prev) => prev.filter((p) => p.id !== leftId));
                setInterimByMember((prev) => {
                  const next = { ...prev };
                  delete next[leftId];
                  return next;
                });
              }
              break;
            }
            case "audio_text": {
              const key = String(msg.from_member_id ?? "unknown");
              setInterimByMember((prev) => ({
                ...prev,
                [key]: {
                  memberId: msg.from_member_id ?? null,
                  fromName: msg.from_name ?? "참가자",
                  text: msg.text ?? "",
                  timestamp: msg.timestamp ?? new Date().toISOString(),
                },
              }));
              break;
            }
            case "speech": {
              const key = String(msg.from_member_id ?? "unknown");
              setInterimByMember((prev) => {
                if (!(key in prev)) return prev;
                const next = { ...prev };
                delete next[key];
                return next;
              });
              setTranscripts((prev) => [
                ...prev,
                {
                  id: `${key}-${msg.timestamp ?? Date.now()}-${prev.length}`,
                  memberId: msg.from_member_id ?? null,
                  fromName: msg.from_name ?? "참가자",
                  text: msg.text ?? "",
                  timestamp: msg.timestamp ?? new Date().toISOString(),
                  isFinal: true,
                },
              ]);
              break;
            }
            case "meeting_ended": {
              setIsMeetingEnded(true);
              break;
            }
            default:
              break;
          }
        } catch {
          // ignore non-JSON frames
        }
      };

      ws.onerror = () => {
        // onerror 단독으로는 재연결 트리거하지 않음 (onclose 가 항상 뒤따라오므로 거기서 처리)
        if (cancelled) return;
      };

      ws.onclose = (ev) => {
        if (cancelled) return;
        setIsConnected(false);

        // 정상 종료(1000) + 의도적 reason 이면 재연결하지 않음
        if (ev.code === 1000 && INTENTIONAL_CLOSE_REASONS.has(ev.reason)) {
          return;
        }

        // 재시도 한도 초과 시 에러만 노출
        if (attempt >= MAX_RETRIES) {
          setErrorMessage("WebSocket 연결 오류");
          return;
        }

        attempt += 1;
        setRetryAttempt(attempt);
        reconnectTimer = setTimeout(() => {
          if (!cancelled) connect();
        }, RETRY_DELAY_MS);
      };
    };

    connect();

    // 새로고침/탭 닫기 시 명시적 close 프레임 송신 — 백엔드가 즉시 disconnect 인지하도록
    const handlePageHide = () => {
      const ws = wsRef.current;
      if (!ws) return;
      try {
        ws.close(1000, "page hide");
      } catch {
        /* noop */
      }
    };
    window.addEventListener("pagehide", handlePageHide);

    return () => {
      cancelled = true;
      if (reconnectTimer) clearTimeout(reconnectTimer);
      window.removeEventListener("pagehide", handlePageHide);
      const ws = wsRef.current;
      if (ws) {
        try {
          ws.close(1000, "unmount");
        } catch {
          /* noop */
        }
      }
      wsRef.current = null;
      setIsConnected(false);
      setTranscripts([]);
      setInterimByMember({});
      setIsMeetingEnded(false);
    };
  }, [meetingId, displayName]);

  return {
    participants,
    isConnected,
    errorMessage,
    retryAttempt,
    transcripts,
    interimByMember,
    sendMessage,
    manualRetry,
    isMeetingEnded,
  };
};

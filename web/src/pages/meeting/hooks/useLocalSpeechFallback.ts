import { useCallback, useEffect, useState } from "react";
import type {
  InterimEntry,
  OutgoingMessageType,
  TranscriptEntry,
} from "../types";

interface UseLocalSpeechFallbackOptions {
  isWsConnected: boolean;
  isMicEnabled: boolean;
  sendMessage: (type: OutgoingMessageType, text: string) => boolean;
}

/**
 * 음성 인식 결과를 처리한다.
 * - WS 연결됨: 백엔드로 전송 (echo 메시지로 transcript에 반영)
 * - WS 미연결: 로컬 state에 즉시 추가하여 UI에 표시
 * - 마이크 음소거: 로컬 interim 즉시 클리어
 */
export const useLocalSpeechFallback = ({
  isWsConnected,
  isMicEnabled,
  sendMessage,
}: UseLocalSpeechFallbackOptions) => {
  const [localTranscripts, setLocalTranscripts] = useState<TranscriptEntry[]>(
    [],
  );
  const [localInterim, setLocalInterim] = useState<string>("");

  // WS가 연결되면 로컬 interim 초기화 (WS echo가 담당)
  useEffect(() => {
    if (isWsConnected) {
      setLocalInterim("");
    }
  }, [isWsConnected]);

  // 마이크 음소거 시 로컬 interim 즉시 제거
  useEffect(() => {
    if (!isMicEnabled) {
      setLocalInterim("");
    }
  }, [isMicEnabled]);

  const handleInterim = useCallback(
    (text: string) => {
      const sent = sendMessage("audio_text", text);
      if (!sent) {
        setLocalInterim(text);
      }
    },
    [sendMessage],
  );

  const handleFinal = useCallback(
    (text: string) => {
      setLocalInterim("");
      const sent = sendMessage("speech", text);
      if (!sent) {
        setLocalTranscripts((prev) => [
          ...prev,
          {
            id: `local-${Date.now()}-${prev.length}`,
            memberId: null,
            fromName: "나",
            text,
            timestamp: new Date().toISOString(),
            isFinal: true,
          },
        ]);
      }
    },
    [sendMessage],
  );

  /** WS transcripts와 로컬 transcripts 병합 (로컬이 먼저 발화된 순) */
  const mergeTranscripts = useCallback(
    (wsTranscripts: TranscriptEntry[]): TranscriptEntry[] => [
      ...localTranscripts,
      ...wsTranscripts,
    ],
    [localTranscripts],
  );

  /** WS interimByMember에 로컬 interim 합성 (WS 미연결 시에만) */
  const mergeInterim = useCallback(
    (wsInterim: Record<string, InterimEntry>): Record<string, InterimEntry> => {
      if (!localInterim || isWsConnected) return wsInterim;
      return {
        ...wsInterim,
        self: {
          memberId: null,
          fromName: "나",
          text: localInterim,
          timestamp: new Date().toISOString(),
        },
      };
    },
    [localInterim, isWsConnected],
  );

  return {
    handleInterim,
    handleFinal,
    mergeTranscripts,
    mergeInterim,
  };
};

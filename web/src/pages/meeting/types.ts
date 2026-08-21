export interface RoomParticipant {
  id: string;
  name: string;
  isSelf?: boolean;
  profileImage?: string | null;
  isSpeaking?: boolean;
}

export interface TranscriptEntry {
  id: string;
  memberId: number | null;
  fromName: string;
  text: string;
  timestamp: string;
  isFinal: boolean;
}

export interface InterimEntry {
  memberId: number | null;
  fromName: string;
  text: string;
  timestamp: string;
}

export type OutgoingMessageType = "chat" | "audio_text" | "speech";

/** 서버가 targetMemberId 로 1:1 중계하는 WebRTC 시그널링 타입 */
export type SignalMessageType = "offer" | "answer" | "ice";

export interface SignalPayload {
  sdp?: RTCSessionDescriptionInit;
  candidate?: RTCIceCandidateInit;
}

export interface SignalFrame {
  type: SignalMessageType;
  fromMemberId: number;
  payload: SignalPayload;
}

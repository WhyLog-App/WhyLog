export interface RoomParticipant {
  id: string;
  name: string;
  isSelf?: boolean;
  profileImage?: string | null;
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

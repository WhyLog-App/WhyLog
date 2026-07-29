export interface CreateMeetingRequest {
  name: string;
  start_date_time: string;
}

export interface CreateMeetingResult {
  meeting_id: number;
  name: string;
  start_date_time: string;
  members: number[] | null;
}

export interface EndMeetingResult {
  meeting_id: number;
  end_date_time: string;
}

export interface DeleteMeetingResult {
  meeting_id: number;
  is_removed: boolean;
}

export type MeetingStatus = "ONGOING" | "COMPLETED";

export interface MeetingListItem {
  meeting_id: number;
  name: string;
  status: MeetingStatus;
  elapse: string | null;
}

export interface MeetingMember {
  member_id: number;
  name: string;
  profile_image: string | null;
}

export interface MeetingDetail {
  meeting_id: number;
  name: string;
  start_date_time: string;
  end_date_time: string | null;
  duration: number | null;
  member_count: number;
  members: MeetingMember[];
}

export interface MeetingAnalysis {
  analysis_id: number;
  meeting_id: number;
  is_analyzed: boolean;
  meeting_title: string | null;
  meeting_purpose: string | null;
  audio_duration: number | null;
  topics: string[];
  core_context: string[];
  application_titles: string[];
  application_reasons: string[];
}

export interface MeetingAudio {
  meeting_id: number;
  audio_key: string;
  audio_url: string;
  audio_duration: number | null;
}

export interface MeetingHistoryDialogue {
  member_id: number;
  content: string;
  timestamp: string;
}

export interface MeetingHistory {
  participants: MeetingMember[];
  dialogues: MeetingHistoryDialogue[];
}

export interface RtcTokenResult {
  meeting_id: number;
  room_name: string;
  server_url: string;
  token: string;
}

export interface CompletedTranscriptItem {
  id: string;
  name: string;
  time: string;
  text: string;
  profile_image: string | null;
}

export interface CompletedMeetingPanels {
  basicInfo: string[];
  topics: string[];
  context: string[];
  finalDecisions: string[];
  rationale: string[];
  misc: string[];
}

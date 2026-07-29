import ENDPOINT from "@/constants/endpoint";
import type { ApiResponse } from "@/types/auth";
import type {
  CreateMeetingRequest,
  CreateMeetingResult,
  DeleteMeetingResult,
  EndMeetingResult,
  MeetingAnalysis,
  MeetingAudio,
  MeetingDetail,
  MeetingHistory,
  MeetingListItem,
  MeetingStatus,
  RtcTokenResult,
} from "@/types/meeting";
import { http } from "@/utils/http";

export const createMeeting = async (
  teamId: number,
  payload: CreateMeetingRequest,
): Promise<CreateMeetingResult> => {
  const { data } = await http.post<
    CreateMeetingRequest,
    { data: ApiResponse<CreateMeetingResult> }
  >(ENDPOINT.MEETINGS.CREATE(teamId), payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const listMeetings = async (
  teamId: number,
  status?: MeetingStatus,
): Promise<MeetingListItem[]> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<MeetingListItem[]> }
  >(ENDPOINT.MEETINGS.LIST(teamId), {
    params: status ? { status } : undefined,
  });
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getMeetingDetail = async (
  meetingId: number,
): Promise<MeetingDetail> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<MeetingDetail> }
  >(ENDPOINT.MEETINGS.DETAIL(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getRtcToken = async (
  meetingId: number,
): Promise<{ serverUrl: string; token: string; roomName: string }> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<RtcTokenResult> }
  >(ENDPOINT.MEETINGS.RTC_TOKEN(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return {
    serverUrl: data.result.server_url,
    token: data.result.token,
    roomName: data.result.room_name,
  };
};

export const getMeetingAnalysis = async (
  meetingId: number,
): Promise<MeetingAnalysis> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<MeetingAnalysis> }
  >(ENDPOINT.MEETINGS.ANALYSIS(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getMeetingAudio = async (
  meetingId: number,
): Promise<MeetingAudio> => {
  const { data } = await http.get<unknown, { data: ApiResponse<MeetingAudio> }>(
    ENDPOINT.MEETINGS.AUDIO(meetingId),
  );
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getMeetingHistory = async (
  meetingId: number,
): Promise<MeetingHistory> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<MeetingHistory> }
  >(ENDPOINT.MEETINGS.HISTORY(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const endMeeting = async (
  meetingId: number,
): Promise<EndMeetingResult> => {
  const { data } = await http.patch<
    undefined,
    { data: ApiResponse<EndMeetingResult> }
  >(ENDPOINT.MEETINGS.END(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const deleteMeeting = async (
  meetingId: number,
): Promise<DeleteMeetingResult> => {
  const { data } = await http.delete<
    unknown,
    { data: ApiResponse<DeleteMeetingResult> }
  >(ENDPOINT.MEETINGS.DELETE(meetingId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

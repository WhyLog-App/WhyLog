package com.whylog.server.domain.meeting.service;

public interface MeetingSpeakerResolver {

    Long resolveMemberIdBySpeakerId(Long meetingId, String speakerId);
}

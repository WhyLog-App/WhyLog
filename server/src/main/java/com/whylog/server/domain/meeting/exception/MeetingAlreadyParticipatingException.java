package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MeetingAlreadyParticipatingException extends GeneralException {

    public MeetingAlreadyParticipatingException() {
        super(MeetingErrorCode.MEETING_ALREADY_PARTICIPATING);
    }
}

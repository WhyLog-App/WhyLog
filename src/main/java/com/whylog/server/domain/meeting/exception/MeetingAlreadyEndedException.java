package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MeetingAlreadyEndedException extends GeneralException {

    public MeetingAlreadyEndedException() {
        super(MeetingErrorCode.MEETING_ALREADY_ENDED);
    }
}

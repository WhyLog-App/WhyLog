package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MeetingNotFoundException extends GeneralException {

    public MeetingNotFoundException() {
        super(MeetingErrorCode.MEETING_NOT_FOUND);
    }
}

package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MeetingInvalidMemberException extends GeneralException {
    public MeetingInvalidMemberException() {
        super(MeetingErrorCode.MEETING_INVALID_MEMBER);
    }
}

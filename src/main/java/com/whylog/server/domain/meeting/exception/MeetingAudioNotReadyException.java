package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MeetingAudioNotReadyException extends GeneralException {

    public MeetingAudioNotReadyException() {
        super(MeetingErrorCode.MEETING_AUDIO_NOT_READY);
    }
}

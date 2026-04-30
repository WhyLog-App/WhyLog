package com.whylog.server.domain.meeting.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode implements BaseErrorCode {

    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_404", "존재하지 않는 회의입니다."),
    MEETING_ALREADY_ENDED(HttpStatus.CONFLICT, "MEETING_409", "이미 종료된 회의입니다."),
    MEETING_INVALID_MEMBER(HttpStatus.CONFLICT, "MEETING_410", "회의에 소속된 참여자가 아닙니다."),
    MEETING_NOT_OWNER(HttpStatus.FORBIDDEN, "MEETING_403", "회의 삭제 권한이 없습니다."),
    MEETING_AUDIO_NOT_READY(HttpStatus.CONFLICT, "MEETING_411", "회의 녹음본이 아직 생성되지 않았거나 업로드가 완료되지 않았습니다."),
    MEETING_ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "MEETING_412", "이미 실시간으로 참여 중인 회의입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}

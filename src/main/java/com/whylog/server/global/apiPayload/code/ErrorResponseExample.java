package com.whylog.server.global.apiPayload.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "에러 응답 예시 스키마")
public class ErrorResponseExample {

    @Schema(description = "요청 성공 여부", name = "isSuccess")
    private boolean isSuccess;

    @Schema(description = "에러 코드")
    private String code;

    @Schema(description = "에러 메시지")
    private String message;

}

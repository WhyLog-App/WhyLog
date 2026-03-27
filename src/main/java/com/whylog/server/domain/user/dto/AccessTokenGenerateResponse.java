package com.whylog.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessTokenGenerateResponse(
        @Schema(description = "새 액세스 토큰")
        String accessToken
) {
    public static AccessTokenGenerateResponse from(String accessToken) {
        return new AccessTokenGenerateResponse(accessToken);
    }
}

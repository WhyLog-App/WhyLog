package com.whylog.server.global.external.fast.dto;

public record FastApiResponse<T>(
        Boolean isSuccess,
        String code,
        String message,
        T result
) {
}

package com.whylog.server.global.external.fast.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record FastApiResponse<T>(
        Boolean isSuccess,
        String code,
        String message,
        T result
) {
}

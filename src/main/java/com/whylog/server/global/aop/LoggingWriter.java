package com.whylog.server.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class LoggingWriter {

    public void logApiStart(
            String requestId,
            String memberInfo,
            String apiName,
            String httpMethod,
            String requestUri,
            LocalDateTime startAt
    ) {
        log.info(
                LoggingMessages.API_START.template(),
                requestId,
                memberInfo,
                apiName,
                httpMethod,
                requestUri,
                startAt
        );
    }

    public void logApiEnd(
            String requestId,
            String memberInfo,
            String apiName,
            String httpMethod,
            String requestUri,
            LocalDateTime endAt,
            long durationMs,
            Integer status,
            String outcome
    ) {
        log.info(
                LoggingMessages.API_END.template(),
                requestId,
                memberInfo,
                apiName,
                httpMethod,
                requestUri,
                endAt,
                durationMs,
                status,
                outcome
        );
    }

    public void logMethodStart(
            String requestId,
            String methodId,
            String memberInfo,
            String methodName,
            LocalDateTime startAt
    ) {
        log.info(
                LoggingMessages.METHOD_START.template(),
                requestId,
                methodId,
                memberInfo,
                methodName,
                startAt
        );
    }

    public void logMethodEnd(
            String requestId,
            String methodId,
            String memberInfo,
            String methodName,
            LocalDateTime endAt,
            long durationMs,
            String outcome
    ) {
        log.info(
                LoggingMessages.METHOD_END.template(),
                requestId,
                methodId,
                memberInfo,
                methodName,
                endAt,
                durationMs,
                outcome
        );
    }
}

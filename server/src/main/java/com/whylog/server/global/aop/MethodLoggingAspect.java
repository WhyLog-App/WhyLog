package com.whylog.server.global.aop;

import com.whylog.server.global.aop.annotation.LogExecution;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class MethodLoggingAspect {

    private static final String REQUEST_TRACE_ID_ATTRIBUTE = "apiRequestTraceId";

    private final LoggingWriter loggingWriter;

    @Around("com.whylog.server.global.aop.Pointcuts.logExecutionMethods(logExecution)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        String methodTraceId = UUID.randomUUID().toString();
        String requestTraceId = resolveRequestTraceId();
        LocalDateTime startAt = LocalDateTime.now();
        long startNano = System.nanoTime();

        String memberInfo = resolveMemberInfo();
        String methodName = resolveMethodName(joinPoint);

        loggingWriter.logMethodStart(requestTraceId, methodTraceId, memberInfo, methodName, startAt);

        Throwable failure = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            LocalDateTime endAt = LocalDateTime.now();
            long elapsedMs = (System.nanoTime() - startNano) / 1_000_000;
            String outcome = failure == null ? "SUCCESS" : "FAILURE";

            loggingWriter.logMethodEnd(requestTraceId, methodTraceId, memberInfo, methodName, endAt, elapsedMs, outcome);
        }
    }

    private String resolveRequestTraceId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            Object requestTraceId = servletRequestAttributes.getRequest().getAttribute(REQUEST_TRACE_ID_ATTRIBUTE);
            if (requestTraceId != null) {
                return requestTraceId.toString();
            }
        }
        return "-";
    }

    private String resolveMethodName(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature methodSignature) {
            return methodSignature.getDeclaringType().getSimpleName() + "." + methodSignature.getName();
        }
        return signature.toShortString();
    }

    private String resolveMemberInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        Object principal = authentication.getPrincipal();
        return principal == null ? "anonymous" : principal.toString();
    }
}

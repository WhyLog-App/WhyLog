package com.whylog.server.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private static final String REQUEST_TRACE_ID_ATTRIBUTE = "apiRequestTraceId";

    private final LoggingWriter loggingWriter;

    @Around("com.whylog.server.global.aop.Pointcuts.restControllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes requestAttributes = currentRequestAttributes();
        if (requestAttributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String requestTraceId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_TRACE_ID_ATTRIBUTE, requestTraceId);

        LocalDateTime startAt = LocalDateTime.now();
        long startNano = System.nanoTime();
        String memberInfo = resolveMemberInfo();
        String apiName = resolveApiName(joinPoint);
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();

        loggingWriter.logApiStart(requestTraceId, memberInfo, apiName, httpMethod, requestUri, startAt);

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
            Integer status = failure == null
                    ? (response != null ? response.getStatus() : null)
                    : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            loggingWriter.logApiEnd(requestTraceId, memberInfo, apiName, httpMethod, requestUri, endAt, elapsedMs, status, outcome);
        }
    }

    private ServletRequestAttributes currentRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes;
        }
        return null;
    }

    private String resolveApiName(ProceedingJoinPoint joinPoint) {
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

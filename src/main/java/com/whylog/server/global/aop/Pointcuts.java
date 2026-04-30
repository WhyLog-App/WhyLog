package com.whylog.server.global.aop;

import com.whylog.server.global.aop.annotation.LogExecution;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Pointcuts {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    @Pointcut("@annotation(logExecution)")
    public void logExecutionMethods(LogExecution logExecution) {
    }
}

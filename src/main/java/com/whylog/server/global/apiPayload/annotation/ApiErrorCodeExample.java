package com.whylog.server.global.apiPayload.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorCodeExamples.class)
public @interface ApiErrorCodeExample {

    Class<? extends Enum<?>> value();
    String name();
}

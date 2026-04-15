package com.whylog.server.global.apiPayload.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

    ApiErrorCodeExample[] value();
}

package com.whylog.server.global.config;

import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

@Slf4j
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI FindCrimeAPI() {
        Info info = new Info()
                .title("WhyLog API")
                .description("WhyLog API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            // 여러 에러 코드가 적용된 경우
            ApiErrorCodeExamples errorAnnotations = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
            if (errorAnnotations != null) {
                for (ApiErrorCodeExample e : errorAnnotations.value()) {
                    handleErrorCode(operation, e.value(), e.name());
                }
            } else {
                // 단일 에러 코드가 적용된 경우
                ApiErrorCodeExample single = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
                if (single != null) {
                    handleErrorCode(operation, single.value(), single.name());
                }
            }

            return operation;
        };
    }

    private void handleErrorCode(Operation operation, Class<? extends Enum<?>> enumClass, String name) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(name) && constant instanceof BaseErrorCode) {
                BaseErrorCode errorCode = (BaseErrorCode) constant;
                generateSingleErrorCodeResponseExample(operation, errorCode);
                break;
            }
        }
    }

    private void generateSingleErrorCodeResponseExample(Operation operation, BaseErrorCode errorCode) {
        String httpStatusCode = String.valueOf(errorCode.getReasonHttpStatus().getHttpStatus().value());
        String code = errorCode.getReasonHttpStatus().getCode();
        String message = errorCode.getReasonHttpStatus().getMessage();

        String exampleJson = String.format("""
        {
          "isSuccess": false,
          "code": "%s",
          "message": "%s"
        }
        """, code, message);

        io.swagger.v3.oas.models.responses.ApiResponse apiResponse =
                operation.getResponses().computeIfAbsent(httpStatusCode, statusCode ->
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description(message)
                                .content(new Content()));

        MediaType mediaType = apiResponse.getContent()
                .computeIfAbsent("application/json", k -> new MediaType());

        mediaType.addExamples(code, new Example().value(exampleJson));
    }
}


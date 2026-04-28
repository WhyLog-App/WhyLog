package com.whylog.server.global.external.fast.client;

import java.net.URI;
import java.util.Map;

import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.FastApiRequestBodyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class FastApiClient {

    private final RestClient restClient;

    @Value("${fast.api.base-url}")
    private String baseUrl;

    protected FastApiClient() {
        this.restClient = RestClient.create();
    }

    protected <T> T get(FastApiInfo fastApiInfo,
                        @Nullable Map<String, ?> pathVariables,
                        ParameterizedTypeReference<T> responseType) {
        return execute(fastApiInfo, pathVariables, null, null, responseType);
    }

    protected <T> T postJson(FastApiInfo fastApiInfo,
                             Object request,
                             ParameterizedTypeReference<T> responseType) {
        return execute(fastApiInfo, Map.of(), request, null, responseType);
    }

    protected <T> T postMultipart(FastApiInfo fastApiInfo,
                                  @Nullable Map<String, ?> textParts,
                                  @Nullable FastApiBinaryPart binaryPart,
                                  ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, HttpEntity<?>> multipartBody = buildMultipartBody(textParts, binaryPart);
        return execute(fastApiInfo, Map.of(), null, multipartBody, responseType);
    }

    private <T> T execute(FastApiInfo fastApiInfo,
                          @Nullable Map<String, ?> pathVariables,
                          @Nullable Object jsonBody,
                          @Nullable MultiValueMap<String, HttpEntity<?>> multipartBody,
                          ParameterizedTypeReference<T> responseType) {
        URI uri = buildUri(fastApiInfo, pathVariables);
        HttpMethod method = fastApiInfo.getMethod();

        log.info("FastAPI request: {} {}", method, uri);

        RestClient.RequestBodyUriSpec requestSpec = restClient.method(method);
        RestClient.RequestBodySpec bodySpec = requestSpec.uri(uri);

        if (fastApiInfo.getFastApiRequestBodyType() == FastApiRequestBodyType.MULTIPART) {
            if (multipartBody == null) {
                throw new IllegalArgumentException("Multipart request body is required for " + fastApiInfo.name());
            }
            bodySpec.contentType(MediaType.MULTIPART_FORM_DATA);
            return bodySpec.body(multipartBody)
                    .retrieve()
                    .body(responseType);
        }

        if (fastApiInfo.getFastApiRequestBodyType() == FastApiRequestBodyType.JSON) {
            bodySpec.contentType(MediaType.APPLICATION_JSON);
        }
        if (jsonBody != null) {
            return bodySpec.body(jsonBody)
                    .retrieve()
                    .body(responseType);
        }

        return bodySpec.retrieve().body(responseType);
    }

    private URI buildUri(FastApiInfo fastApiInfo, @Nullable Map<String, ?> pathVariables) {
        Map<String, ?> variables = pathVariables == null ? Map.of() : pathVariables;
        return UriComponentsBuilder.fromHttpUrl(trimTrailingSlash(baseUrl))
                .path(fastApiInfo.getPath())
                .buildAndExpand(variables)
                .toUri();
    }

    private MultiValueMap<String, HttpEntity<?>> buildMultipartBody(@Nullable Map<String, ?> textParts,
                                                                    @Nullable FastApiBinaryPart binaryPart) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        if (textParts != null) {
            textParts.forEach((key, value) -> {
                if (value != null) {
                    builder.part(key, value);
                }
            });
        }

        if (binaryPart != null) {
            MultipartBodyBuilder.PartBuilder partBuilder = builder.part(binaryPart.partName(), binaryPart.resource());
            if (StringUtils.hasText(binaryPart.filename())) {
                partBuilder.filename(binaryPart.filename());
            }
            if (StringUtils.hasText(binaryPart.contentType())) {
                partBuilder.contentType(MediaType.parseMediaType(binaryPart.contentType()));
            }
        }

        return new LinkedMultiValueMap<>(builder.build());
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

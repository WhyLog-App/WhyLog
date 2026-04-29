package com.whylog.server.global.external.fast.client;

import java.util.Map;

import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class FastApiSystemClient extends FastApiClient {

    public FastApiResponse<Map<String, String>> readRoot() {
        return get(
                FastApiInfo.READ_ROOT,
                Map.of(),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<Map<String, String>> healthCheck() {
        return get(
                FastApiInfo.HEALTH_CHECK,
                Map.of(),
                new ParameterizedTypeReference<>() {
                }
        );
    }
}

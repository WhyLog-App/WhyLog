package com.whylog.server.global.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.web.socket.TextMessage;

public class JsonConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    // keyword 없을 때
    public static String toJson(Object value, String keyword) {

        String failKeyword = keyword != null ? keyword : "객체 -> Json 변환 실패";

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(failKeyword, exception);
        }
    }

    // keyword 있을 때
    public static String toJson(Object value) {
        return toJson(value, null);
    }

    // 객체를 원하는 타입으로 역직렬화할 때
    public static <T> T readValue(TextMessage message, Class<T> returnType) throws JsonProcessingException {
        return objectMapper.readValue(message.getPayload(), returnType);
    }
}

package com.whylog.server.global.external.livekit;

import com.whylog.server.global.util.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitEgressClient {

    @Value("${livekit.url}")
    private String liveKitUrl;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${aws.s3.force-path-style:false}")
    private boolean forcePathStyle;

    private final RestClient restClient = RestClient.create();

    public String startRoomAudioEgress(String egressToken, String roomName, String recordingKey) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("room_name", roomName);
        request.put("audio_only", true);
        request.put("file_outputs", List.of(buildFileOutput(recordingKey)));

        Map<String, Object> response = post("/twirp/livekit.Egress/StartRoomCompositeEgress", egressToken, request);
        Object egressId = response.get("egress_id");
        if (egressId == null) {
            throw new IllegalStateException("LiveKit egress response did not include egress_id");
        }
        return egressId.toString();
    }

    public void createRoom(String roomCreateToken, String roomName) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("name", roomName);

        try {
            post("/twirp/livekit.RoomService/CreateRoom", roomCreateToken, request);
        } catch (HttpClientErrorException.Conflict e) {
            log.info("LiveKit room already exists: roomName={}", roomName);
        }
    }

    public void stopEgress(String egressToken, String egressId) {
        Map<String, Object> request = Map.of("egress_id", egressId);
        post("/twirp/livekit.Egress/StopEgress", egressToken, request);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, String egressToken, Map<String, Object> request) {
        String apiBaseUrl = toHttpsBaseUrl(liveKitUrl);
        URI uri = URI.create(apiBaseUrl + path);

        Object body = request;
        String requestJson = JsonConverter.toJson(request);
        log.info("LiveKit egress request: {}", requestJson);

        Map<String, Object> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + egressToken)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("LiveKit egress response is empty");
        }

        log.info("LiveKit egress response: {}", JsonConverter.toJson(response));
        return response;
    }

    private Map<String, Object> buildFileOutput(String recordingKey) {
        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("access_key", accessKey);
        s3.put("secret", secretKey);
        s3.put("bucket", bucket);
        s3.put("region", region);
        if (StringUtils.hasText(s3Endpoint)) {
            s3.put("endpoint", s3Endpoint);
        }
        s3.put("force_path_style", forcePathStyle);

        Map<String, Object> fileOutput = new LinkedHashMap<>();
        fileOutput.put("filepath", recordingKey);
        fileOutput.put("s3", s3);
        return fileOutput;
    }

    private String toHttpsBaseUrl(String wsUrl) {
        if (wsUrl.startsWith("wss://")) {
            return "https://" + wsUrl.substring("wss://".length());
        }
        if (wsUrl.startsWith("ws://")) {
            return "http://" + wsUrl.substring("ws://".length());
        }
        if (wsUrl.startsWith("https://") || wsUrl.startsWith("http://")) {
            return wsUrl;
        }
        return "https://" + wsUrl;
    }

}

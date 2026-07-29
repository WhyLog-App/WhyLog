package com.whylog.server.global.external.s3;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Client {

    private final software.amazon.awssdk.services.s3.S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadFile(MultipartFile file, ImageType imageType) {

        if (file == null || file.isEmpty()) {
            throw new S3Exception(S3ErrorCode.S3_FILE_EMPTY);
        }

        if (!StringUtils.hasText(bucket)) {
            throw new S3Exception(S3ErrorCode.S3_BUCKET_NOT_CONFIGURED);
        }

        if (file.isEmpty()) {
            throw new S3Exception(S3ErrorCode.S3_FILE_EMPTY);
        }

        String key = getImageFileName(file, imageType);

        if (!StringUtils.hasText(key)) {
            throw new S3Exception(S3ErrorCode.S3_FILE_NAME_EMPTY);
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | software.amazon.awssdk.services.s3.model.S3Exception | SdkClientException e) {
            log.error("S3 에러 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAILED);
        }

        return key;
    }


    // 이미지 가져옴
    public String getFileUrl(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }

        String encodedFileName = encodeS3Key(fileName);
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + encodedFileName;
    }

    public String getPresignedFileUrl(String fileName, Duration duration, String responseContentType) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }

        GetObjectRequest.Builder getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName);

        if (StringUtils.hasText(responseContentType)) {
            getObjectRequest.responseContentType(responseContentType);
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest.build())
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public boolean exists(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
            return true;
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == 404 || e.statusCode() == 403) {
                return false;
            }
            throw e;
        } catch (SdkClientException e) {
            log.error("S3 존재 확인 에러 발생: {}", e.getMessage());
            return false;
        }
    }

    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }

        if (!StringUtils.hasText(bucket)) {
            throw new S3Exception(S3ErrorCode.S3_BUCKET_NOT_CONFIGURED);
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception | SdkClientException e) {
            log.error("S3 삭제 에러 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.S3_DELETE_FAILED);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    // 파일 이름 생성
    private String getImageFileName(MultipartFile image, ImageType imageType) {
        return S3KeyGenerator.makeImageKey(image.getOriginalFilename(), imageType);
    }

    private String encodeS3Key(String key) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");
    }

}

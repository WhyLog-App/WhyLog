package com.whylog.server.global.external.s3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class S3KeyGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

    private S3KeyGenerator() {
    }

    // 이미지 유형 접두사 + 현재날짜시각( xxxx-xx-xx )
    public static String makeImageKey(String fileName, ImageType imageType) {
        return makeImageKey(fileName, imageType, null);
    }

    // 이미지 유형 접두사 + 객체id(선택) + 현재날짜시각( xxxx-xx-xx )
    public static String makeImageKey(String fileName, ImageType imageType, Long objectId) {
        StringBuilder key = new StringBuilder(imageType.getPrefix());

        if (objectId != null) {
            key.append(objectId).append("_");
        }

        key.append(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        key.append(extractExtension(fileName));

        return key.toString();
    }

    private static String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

}

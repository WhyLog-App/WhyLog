package com.whylog.server.global.external.s3;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3Client {

    public String uploadFile(String fileName, MultipartFile file){
        return null;
    }

    public String getFileUrl(String fileName){
        return null;
    }

}
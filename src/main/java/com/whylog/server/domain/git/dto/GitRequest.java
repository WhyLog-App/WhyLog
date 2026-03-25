package com.whylog.server.domain.git.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GitRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 추가 요청")
    public static class RepositoryCreateDTO {

        @Schema(description = "레포 이름", example = "whyLog-BE")
        private String name;

        @Schema(description = "레포 URL", example = "https://github.com/WhyLog-App/WhyLog-BE")
        private String url;
    }

}


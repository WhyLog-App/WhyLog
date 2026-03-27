package com.whylog.server.domain.git.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.URL;

public class GitRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 추가 요청")
    public static class RepositoryCreateDTO {

        @Schema(description = "레포 이름", example = "whyLog-BE")
        @NotBlank
        private String name;

        @Schema(description = "레포 URL", example = "https://github.com/WhyLog-App/WhyLog-BE")
        @NotBlank @URL
        private String url;
    }

}


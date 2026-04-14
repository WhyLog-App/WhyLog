package com.whylog.server.global.external.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {

    TEAM_IMAGE("team_image_"),
    MEMBER_PROFILE("member_profile_image_"),
    ;

    private final String prefix;

}

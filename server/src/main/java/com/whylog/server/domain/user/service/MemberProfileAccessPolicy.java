package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberProfileAccessPolicy {

    public boolean canViewActivity(Long viewerId, Member profileMember) {
        return viewerId.equals(profileMember.getId())
                || profileMember.getProfileVisibility().isPublic();
    }
}

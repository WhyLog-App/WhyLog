package com.whylog.server.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER");

    private final String roleName;

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority(roleName);
    }
}

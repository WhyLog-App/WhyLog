package com.whylog.server.domain.user.entity;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    private Member(String name, String email, String password, String profileImage, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.role = role;
    }

    public static Member create(AuthRequest.SignUpDTO dto, String password, Role role) {
        return Member.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(password)
                .role(role)
                .build();
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}

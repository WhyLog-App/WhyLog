package com.whylog.server.domain.user.entity;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.enums.ProfileVisibility;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.global.entity.BaseEntity;
import com.whylog.server.global.util.crypto.AESCryptoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false, length = 20)
    private ProfileVisibility profileVisibility;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "purge_at")
    private LocalDateTime purgeAt;

    @Convert(converter = AESCryptoConverter.class)
    @Column(name = "github_access_token", length = 500, nullable = true)
    private String githubAccessToken;

    @Builder
    private Member(
            String name,
            String email,
            String password,
            String profileImage,
            Role role,
            AccountStatus accountStatus,
            ProfileVisibility profileVisibility,
            LocalDateTime emailVerifiedAt,
            LocalDateTime purgeAt) {
        this.name = name;
        this.email = canonicalizeEmail(email);
        this.password = password;
        this.profileImage = profileImage;
        this.role = role;
        this.accountStatus = accountStatus == null ? AccountStatus.UNVERIFIED : accountStatus;
        this.profileVisibility =
                profileVisibility == null ? ProfileVisibility.PUBLIC : profileVisibility;
        this.emailVerifiedAt = emailVerifiedAt;
        this.purgeAt = purgeAt;
    }

    public static Member create(AuthRequest.SignUpDTO dto, String password, Role role) {
        return Member.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(password)
                .role(role)
                .build();
    }

    public static String canonicalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void verifyEmail(LocalDateTime verifiedAt) {
        this.accountStatus = AccountStatus.ACTIVE;
        this.emailVerifiedAt = verifiedAt;
    }

    public void requestWithdrawal(LocalDateTime requestedAt) {
        if (this.accountStatus != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only active members can request withdrawal.");
        }
        this.accountStatus = AccountStatus.INACTIVE;
        this.purgeAt = firstMidnightAfterThirtyDays(requestedAt);
    }

    private LocalDateTime firstMidnightAfterThirtyDays(LocalDateTime requestedAt) {
        LocalDateTime exactExpiry = requestedAt.plusDays(30);
        LocalDateTime midnight = exactExpiry.toLocalDate().atStartOfDay();
        return midnight.isBefore(exactExpiry) ? midnight.plusDays(1) : midnight;
    }

    public void recoverWithdrawal() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.purgeAt = null;
    }

    public void purgeWithdrawal(String syntheticEmail, String encodedPassword) {
        this.name = "탈퇴한 사용자";
        this.email = canonicalizeEmail(syntheticEmail);
        this.password = encodedPassword;
        this.profileImage = null;
        this.githubAccessToken = null;
        this.accountStatus = AccountStatus.WITHDRAW;
        this.purgeAt = null;
    }

    public boolean isWithdrawalGrace() {
        return this.accountStatus == AccountStatus.INACTIVE && this.purgeAt != null;
    }

    public boolean isWithdrawalGraceActive(LocalDateTime now) {
        return isWithdrawalGrace() && this.purgeAt.isAfter(now);
    }

    public boolean isWithdrawalGraceExpired(LocalDateTime now) {
        return isWithdrawalGrace() && !this.purgeAt.isAfter(now);
    }

    public boolean isWithdrawnForRead(LocalDateTime now) {
        return this.accountStatus == AccountStatus.WITHDRAW || isWithdrawalGraceExpired(now);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateProfileVisibility(ProfileVisibility profileVisibility) {
        this.profileVisibility = Objects.requireNonNull(profileVisibility);
    }

    public void removeProfileImage() {
        this.profileImage = null;
    }

    public void setGithubAccessToken(String token) {
        this.githubAccessToken = token;
    }

    // 토큰 존재 확인
    public boolean hasGithubToken() {
        return this.githubAccessToken != null && !this.githubAccessToken.isEmpty();
    }

    // 401 에러시 토큰 삭제
    public void clearGithubToken() {
        this.githubAccessToken = null;
    }
}

package com.whylog.server.domain.user.service;

import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.external.s3.S3Client;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WithdrawalPurgeService {

    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final S3Client s3Client;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void purgeExpiredWithdrawalMembers() {
        purgeExpiredWithdrawalMembers(LocalDateTime.now());
    }

    public int purgeExpiredWithdrawalMembers(LocalDateTime now) {
        List<Member> members =
                memberRepository.findPurgeCandidatesForUpdate(AccountStatus.INACTIVE, now);
        return (int) members.stream().filter(member -> purge(member, now)).count();
    }

    private boolean purge(Member member, LocalDateTime now) {
        if (!member.isWithdrawalGraceExpired(now)) {
            return false;
        }

        String imageKey = member.getProfileImage();
        String syntheticEmail = "withdrawn-" + member.getId() + "@whylog.invalid";
        String encodedPassword = passwordEncoder.encode(randomSecret());
        member.purgeWithdrawal(syntheticEmail, encodedPassword);
        teamMemberRepository.deactivateActiveMembershipsByMemberId(member.getId());
        scheduleAfterCommit(() -> deleteProfileImageBestEffort(imageKey));
        return true;
    }

    private void deleteProfileImageBestEffort(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteFile(imageKey);
        } catch (RuntimeException exception) {
            log.warn("Failed to delete purged member profile image: {}", imageKey, exception);
        }
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            task.run();
                        }
                    });
            return;
        }

        task.run();
    }

    private String randomSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

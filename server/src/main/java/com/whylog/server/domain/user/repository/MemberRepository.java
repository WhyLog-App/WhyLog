package com.whylog.server.domain.user.repository;

import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    boolean existsByIdAndAccountStatus(Long memberId, AccountStatus accountStatus);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByIdAndAccountStatus(Long memberId, AccountStatus accountStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.email = :email")
    Optional<Member> findByEmailForUpdate(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select m
            from Member m
            where m.accountStatus = :accountStatus
              and m.purgeAt <= :purgeAt
            order by m.id asc
            """)
    List<Member> findPurgeCandidatesForUpdate(
            @Param("accountStatus") AccountStatus accountStatus,
            @Param("purgeAt") LocalDateTime purgeAt);
}

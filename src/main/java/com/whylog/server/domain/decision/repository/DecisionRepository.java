package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.Decision;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    Optional<Decision> findByMeetingId(Long meetingId);

    // 결정사항 상세 조회에 필요한 회의와 참여자 정보를 함께 조회한다.
    @Query("""
            SELECT DISTINCT d
            FROM Decision d
            JOIN FETCH d.meeting m
            LEFT JOIN FETCH m.meetingMembers mm
            LEFT JOIN FETCH mm.member
            WHERE d.id = :decisionId
            """)
    Optional<Decision> findDetailById(@Param("decisionId") Long decisionId);

    @Modifying
    @Query("DELETE FROM Application a WHERE a.decision.meeting.team.id = :teamId")
    void deleteApplicationsByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Application a WHERE a.decision.meeting.id = :meetingId")
    void deleteApplicationsByMeetingId(@Param("meetingId") Long meetingId);

    @Modifying
    @Query("DELETE FROM Decision d WHERE d.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Decision d WHERE d.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

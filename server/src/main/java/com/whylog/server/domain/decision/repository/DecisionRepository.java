package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.Decision;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    Optional<Decision> findByMeetingId(Long meetingId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            UPDATE Decision d
            SET d.reliabilityScore = :reliabilityScore
            WHERE d.id = :decisionId
            """)
    void updateReliabilityScore(
            @Param("decisionId") Long decisionId,
            @Param("reliabilityScore") Integer reliabilityScore);

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

    @Query(
            """
            SELECT new com.whylog.server.domain.decision.repository.DecisionRepository$RecentDecisionRow(
              d.id,
              t.id,
              t.name,
              m.name,
              d.createdAt
            )
            FROM Decision d
            JOIN d.meeting m
            JOIN m.team t
            JOIN m.meetingMembers mm
            JOIN TeamMember tm ON tm.team = t AND tm.member.id = :memberId AND tm.active = true
            WHERE mm.member.id = :memberId
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<RecentDecisionRow> findRecentDecisionRowsByMemberId(
            @Param("memberId") Long memberId, org.springframework.data.domain.Pageable pageable);

    record RecentDecisionRow(
            Long decisionId,
            Long projectId,
            String projectName,
            String name,
            LocalDateTime createdAt) {}
}

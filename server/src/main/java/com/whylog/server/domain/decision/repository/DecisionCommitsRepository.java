package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionCommits;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionCommitsRepository extends JpaRepository<DecisionCommits, Long> {

    // 추천 결과 저장시 중복 방지용
    Optional<DecisionCommits> findByDecisionIdAndCommitId(Long decisionId, Long commitId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DecisionCommits dc
            WHERE dc.decision.id = :decisionId
            """)
    void deleteByDecisionId(@Param("decisionId") Long decisionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DecisionCommits dc
            WHERE dc.decision.meeting.team.id = :teamId
            """)
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DecisionCommits dc
            WHERE dc.decision.meeting.id = :meetingId
            """)
    void deleteByMeetingId(@Param("meetingId") Long meetingId);

    @Query("""
            SELECT dc.id
            FROM DecisionCommits dc
            WHERE dc.commitId IN :commitIds
            """)
    List<Long> findIdsByCommitIdIn(@Param("commitIds") List<Long> commitIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DecisionCommits dc
            WHERE dc.commitId IN :commitIds
            """)
    void deleteByCommitIdIn(@Param("commitIds") List<Long> commitIds);
}

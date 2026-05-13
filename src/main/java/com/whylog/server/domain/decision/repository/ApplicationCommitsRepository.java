package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationCommits;
import com.whylog.server.domain.decision.entity.ApplicationCommitsId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationCommitsRepository extends JpaRepository<ApplicationCommits, ApplicationCommitsId> {

    // 적용사항에 추천된 커밋 연결 목록을 최신순으로 조회합니다.
    @Query("""
            SELECT ac
            FROM ApplicationCommits ac
            JOIN FETCH ac.decisionCommits dc
            JOIN Commit c ON c.id = dc.commitId
            WHERE ac.application.id = :applicationId
            ORDER BY c.dateTime DESC, c.id DESC
            """)
    List<ApplicationCommits> findByApplicationId(@Param("applicationId") Long applicationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ApplicationCommits ac
            WHERE ac.application.decision.id = :decisionId
            """)
    void deleteByDecisionId(@Param("decisionId") Long decisionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ApplicationCommits ac
            WHERE ac.decisionCommits.id IN :decisionCommitIds
            """)
    void deleteByDecisionCommitsIdIn(@Param("decisionCommitIds") List<Long> decisionCommitIds);
}

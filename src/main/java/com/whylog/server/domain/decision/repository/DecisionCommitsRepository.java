package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionCommits;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionCommitsRepository extends JpaRepository<DecisionCommits, Long> {

    // 추천 결과 저장시 중복 방지용
    Optional<DecisionCommits> findByDecisionIdAndCommitId(Long decisionId, Long commitId);
}

package com.whylog.server.domain.git.repository;

import com.whylog.server.domain.git.entity.CommitAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitAnalysisRepository extends JpaRepository<CommitAnalysis, Long> {

    // 커밋 ID로 분석 결과를 조회
    Optional<CommitAnalysis> findByCommitId(Long commitId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM CommitAnalysis ca
            WHERE ca.commit.repository.id = :repositoryId
            """)
    void deleteByRepositoryId(@Param("repositoryId") Long repositoryId);
}

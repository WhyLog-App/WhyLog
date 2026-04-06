package com.whylog.server.domain.git.repository;

import com.whylog.server.domain.git.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    // 팀의 레포지토리를 최근 동기화 시간 기준 내림차순으로 조회
    @Query("SELECT r FROM Repository r " +
            "WHERE r.team.id = :teamId " +
            "ORDER BY r.lastSyncedAt DESC NULLS LAST, r.id DESC")
    List<Repository> findByTeamId(@Param("teamId") Long teamId);
}

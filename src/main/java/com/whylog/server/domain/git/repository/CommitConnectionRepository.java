package com.whylog.server.domain.git.repository;

import com.whylog.server.domain.git.entity.CommitConnection;
import com.whylog.server.domain.git.entity.CommitConnectionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitConnectionRepository extends JpaRepository<CommitConnection, CommitConnectionId> {

    // 적용사항에 연결된 커밋 목록을 최신순으로 조회합니다.
    @Query("""
            SELECT cc
            FROM CommitConnection cc
            JOIN FETCH cc.commit c
            JOIN FETCH c.repository r
            WHERE cc.application.id = :applicationId
            ORDER BY c.dateTime DESC, c.id DESC
            """)
    List<CommitConnection> findByApplicationId(@Param("applicationId") Long applicationId);
}

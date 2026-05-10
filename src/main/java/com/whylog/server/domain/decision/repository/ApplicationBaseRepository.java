package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationBaseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationBaseRepository extends JpaRepository<ApplicationBase, ApplicationBaseId> {

    // 적용사항에 연결된 결정근거 목록 조회
    @Query("""
            SELECT ab
            FROM ApplicationBase ab
            JOIN FETCH ab.decisionBase db
            WHERE ab.application.id = :applicationId
            ORDER BY db.id ASC
            """)
    List<ApplicationBase> findByApplicationId(@Param("applicationId") Long applicationId);

    @Modifying
    @Query("DELETE FROM ApplicationBase ab WHERE ab.application.decision.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM ApplicationBase ab WHERE ab.application.decision.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

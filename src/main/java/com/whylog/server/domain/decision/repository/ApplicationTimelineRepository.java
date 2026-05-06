package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.entity.ApplicationTimelineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationTimelineRepository extends JpaRepository<ApplicationTimeline, ApplicationTimelineId> {

    // 적용사항에 연결된 결정 타임라인 목록을 조회한다.
    @Query("""
            SELECT at
            FROM ApplicationTimeline at
            JOIN FETCH at.decisionTimeline dt
            WHERE at.application.id = :applicationId
            ORDER BY dt.timestamp ASC, dt.id ASC
            """)
    List<ApplicationTimeline> findByApplicationId(@Param("applicationId") Long applicationId);

    @Modifying
    @Query("DELETE FROM ApplicationTimeline at WHERE at.application.decision.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM ApplicationTimeline at WHERE at.application.decision.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

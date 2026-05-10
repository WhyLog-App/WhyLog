package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionTimelineRepository extends JpaRepository<DecisionTimeline, Long> {

    @Modifying
    @Query("""
            DELETE FROM DecisionTimeline dt
            WHERE dt.decision.meeting.id = :meetingId
            """)
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

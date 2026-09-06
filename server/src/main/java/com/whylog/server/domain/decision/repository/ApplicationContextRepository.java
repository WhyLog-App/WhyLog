package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationContext;
import com.whylog.server.domain.decision.entity.ApplicationContextId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationContextRepository
        extends JpaRepository<ApplicationContext, ApplicationContextId> {

    @Query(
            """
            SELECT ac
            FROM ApplicationContext ac
            JOIN FETCH ac.decisionContext dc
            WHERE ac.application.id = :applicationId
            ORDER BY dc.timestamp ASC, dc.id ASC
            """)
    List<ApplicationContext> findByApplicationId(@Param("applicationId") Long applicationId);

    @Modifying
    @Query(
            "DELETE FROM ApplicationContext ac WHERE ac.application.decision.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query(
            "DELETE FROM ApplicationContext ac WHERE ac.application.decision.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

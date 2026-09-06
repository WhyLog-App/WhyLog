package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionContextRepository extends JpaRepository<DecisionContext, Long> {

    @Modifying
    @Query("DELETE FROM DecisionContext dc WHERE dc.decision.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM DecisionContext dc WHERE dc.decision.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

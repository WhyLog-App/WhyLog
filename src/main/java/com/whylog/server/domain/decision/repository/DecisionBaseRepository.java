package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionBaseRepository extends JpaRepository<DecisionBase, Long> {

    @Modifying
    @Query("DELETE FROM DecisionBase db WHERE db.decision.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM DecisionBase db WHERE db.decision.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

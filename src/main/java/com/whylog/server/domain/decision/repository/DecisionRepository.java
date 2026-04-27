package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    @Modifying
    @Query("DELETE FROM Application a WHERE a.decision.meeting.team.id = :teamId")
    void deleteApplicationsByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Application a WHERE a.decision.meeting.id = :meetingId")
    void deleteApplicationsByMeetingId(@Param("meetingId") Long meetingId);

    @Modifying
    @Query("DELETE FROM Decision d WHERE d.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Decision d WHERE d.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

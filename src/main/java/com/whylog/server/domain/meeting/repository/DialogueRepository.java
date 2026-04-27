package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    @Modifying
    @Query("DELETE FROM Dialogue d WHERE d.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM Dialogue d WHERE d.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

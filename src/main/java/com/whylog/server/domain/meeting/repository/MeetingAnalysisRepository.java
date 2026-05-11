package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingAnalysisRepository extends JpaRepository<MeetingAnalysis, Long> {

    @Modifying
    @Query("DELETE FROM MeetingAnalysis ma WHERE ma.meeting.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM MeetingAnalysis ma WHERE ma.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);

    @Query("SELECT ma FROM MeetingAnalysis ma WHERE ma.meeting.id = :meetingId")
    Optional<MeetingAnalysis> findByMeetingId(@Param("meetingId") Long meetingId);
}

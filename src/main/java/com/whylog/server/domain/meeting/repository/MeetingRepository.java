package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("""
        SELECT m FROM Meeting m
        LEFT JOIN FETCH Team t
            ON t.id = :teamId
    """)
    List<Meeting> findByTeamId(@Param("teamId") Long teamId);

    @Query("""
        SELECT m FROM Meeting m
        LEFT JOIN FETCH MeetingMember mm
        WHERE m.id = :meetingId
            AND mm.meeting = m
    """)
    Optional<Meeting> findWithMembers(@Param("meetingId") Long meetingId);


}

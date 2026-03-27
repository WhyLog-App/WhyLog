package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import io.swagger.v3.oas.annotations.Parameter;
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
            ON mm.meeting.id = m.id
        WHERE m.id = :meetingId
    """)
    Optional<Meeting> findWithMembers(@Param("meetingId") Long meetingId);


}

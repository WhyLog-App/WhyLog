package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("""
        SELECT m
        FROM Meeting m
        WHERE m.team.id = :teamId
        ORDER BY m.startDateTime DESC
    """)
    List<Meeting> findByTeamId(@Param("teamId") Long teamId);

    @Query("""
        SELECT DISTINCT m
        FROM Meeting m
        LEFT JOIN FETCH m.meetingMembers mm
        LEFT JOIN FETCH mm.member
        WHERE m.id = :meetingId
    """)
    Optional<Meeting> findWithMembers(@Param("meetingId") Long meetingId);

    @Query("""
        SELECT m.id
        FROM Meeting m
        WHERE m.team.id = :teamId
    """)
    List<Long> findIdsByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("""
        DELETE FROM Meeting m
        WHERE m.team.id = :teamId
    """)
    void deleteByTeamId(@Param("teamId") Long teamId);


}

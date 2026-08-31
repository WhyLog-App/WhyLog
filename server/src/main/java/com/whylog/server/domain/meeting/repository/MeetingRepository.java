package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.Meeting;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query(
            """
        SELECT m
        FROM Meeting m
            LEFT JOIN FETCH m.meetingAnalysis
        WHERE m.team.id = :teamId
        ORDER BY m.startDateTime DESC
    """)
    List<Meeting> findWithAnalysis(@Param("teamId") Long teamId);

    @Query(
            """
        SELECT DISTINCT m
        FROM Meeting m
        JOIN FETCH m.team
        LEFT JOIN FETCH m.meetingMembers mm
        LEFT JOIN FETCH mm.member
        WHERE m.id = :meetingId
    """)
    Optional<Meeting> findWithMembers(@Param("meetingId") Long meetingId);

    @Query(
            """
        SELECT m.id
        FROM Meeting m
        WHERE m.team.id = :teamId
    """)
    List<Long> findIdsByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query(
            """
        DELETE FROM Meeting m
        WHERE m.team.id = :teamId
    """)
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Query(
            """
        SELECT m FROM Meeting m
        JOIN FETCH m.team
        LEFT JOIN FETCH m.meetingAnalysis ma
            WHERE m.id = :meetingId
    """)
    Optional<Meeting> findByMeetingId(@Param("meetingId") Long meetingId);

    @Modifying
    @Query(
            """
        UPDATE Meeting m
        SET m.endDateTime = :endedAt
        WHERE m.endDateTime IS NULL
    """)
    int markAllOngoingMeetingsAsEnded(@Param("endedAt") LocalDateTime endedAt);

    @Query(
            """
        SELECT DISTINCT m FROM Meeting m
        JOIN FETCH m.team
        LEFT JOIN FETCH m.dialogues d
        LEFT JOIN FETCH d.member dm
        WHERE m.id = :meetingId
    """)
    Optional<Meeting> findWithDialogue(@Param("meetingId") Long meetingId);

    @Query(
            """
            SELECT new com.whylog.server.domain.meeting.repository.MeetingRepository$ProfileMeetingStatsRow(
              m.team.id,
              count(m.id),
              cast(coalesce(sum(timestampdiff(SECOND, m.startDateTime, m.endDateTime)), 0) as long)
            )
            FROM Meeting m
            JOIN m.meetingMembers mm
            WHERE m.team.id IN :teamIds
              AND mm.member.id = :memberId
              AND m.endDateTime IS NOT NULL
            GROUP BY m.team.id
            """)
    List<ProfileMeetingStatsRow> findMemberCompletedMeetingStats(
            @Param("memberId") Long memberId, @Param("teamIds") List<Long> teamIds);

    @Query(
            """
            SELECT new com.whylog.server.domain.meeting.repository.MeetingRepository$RecentMeetingRow(
              m.id,
              t.id,
              t.name,
              m.name,
              m.endDateTime,
              cast(timestampdiff(SECOND, m.startDateTime, m.endDateTime) as long)
            )
            FROM Meeting m
            JOIN m.team t
            JOIN m.meetingMembers mm
            JOIN TeamMember tm ON tm.team = t AND tm.member.id = :memberId AND tm.active = true
            WHERE mm.member.id = :memberId
              AND m.endDateTime IS NOT NULL
            ORDER BY m.endDateTime DESC, m.id DESC
            """)
    List<RecentMeetingRow> findRecentCompletedMeetingRowsByMemberId(
            @Param("memberId") Long memberId, org.springframework.data.domain.Pageable pageable);

    record ProfileMeetingStatsRow(Long projectId, Long meetingCount, Long durationSeconds) {}

    record RecentMeetingRow(
            Long meetingId,
            Long projectId,
            String projectName,
            String name,
            LocalDateTime endedAt,
            Long durationSeconds) {}
}

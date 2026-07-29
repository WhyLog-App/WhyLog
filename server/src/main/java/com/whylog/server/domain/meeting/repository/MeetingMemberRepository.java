package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.entity.MeetingMemberId;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, MeetingMemberId> {
    boolean existsByMemberIdAndMeetingId(Long memberId, Long meetingId);

    @Query("""
        SELECT mm
        FROM MeetingMember mm
        JOIN FETCH mm.meeting
        WHERE mm.member.id = :memberId
          AND mm.meeting.id = :meetingId
          AND mm.role = :role
    """)
    Optional<MeetingMember> findOwnerMeetingMember(
            @Param("memberId") Long memberId,
            @Param("meetingId") Long meetingId,
            @Param("role") MeetingRole role
    );

    @Modifying
    @Query("""
        DELETE FROM MeetingMember mm
        WHERE mm.meeting.team.id = :teamId
    """)
    void deleteByTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("""
        DELETE FROM MeetingMember mm
        WHERE mm.meeting.id = :meetingId
    """)
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}

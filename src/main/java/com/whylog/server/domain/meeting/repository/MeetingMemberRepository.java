package com.whylog.server.domain.meeting.repository;

import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.entity.MeetingMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, MeetingMemberId> {
    boolean existsByMemberIdAndMeetingId(Long memberId, Long meetingId);
}

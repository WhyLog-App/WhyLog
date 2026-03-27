package com.whylog.server.domain.meeting.entity;

import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Meeting_Member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingMember extends BaseEntity {

    @EmbeddedId
    private MeetingMemberId id;

    @MapsId("meetingId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private MeetingRole role;

    @Builder
    private MeetingMember(MeetingMemberId id, Meeting meeting, Member member, MeetingRole role) {
        this.id = id;
        this.meeting = meeting;
        this.member = member;
        this.role = role;
    }

    public static MeetingMember create(Meeting meeting, Member member, MeetingRole role) {
        return MeetingMember.builder()
                .id(new MeetingMemberId(meeting.getId(), member.getId()))
                .meeting(meeting)
                .member(member)
                .role(role)
                .build();
    }
}

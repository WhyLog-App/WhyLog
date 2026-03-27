package com.whylog.server.domain.team.entity;

import com.whylog.server.domain.team.enums.TeamRole;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Team_Member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseEntity {

    @EmbeddedId
    private TeamMemberId id;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_active")
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private TeamRole role;

    @Builder
    private TeamMember(TeamMemberId id, Team team, Member member, Boolean active, TeamRole role) {
        this.id = id;
        this.team = team;
        this.member = member;
        this.active = active;
        this.role = role;
    }

    public static TeamMember create(Team team, Member member, TeamRole role) {
        return TeamMember.builder()
                .id(TeamMemberId.of(team.getId(), member.getId()))
                .team(team)
                .member(member)
                .active(true)
                .role(role)
                .build();
    }
}

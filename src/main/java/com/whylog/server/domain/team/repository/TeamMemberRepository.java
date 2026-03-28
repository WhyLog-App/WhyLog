package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.entity.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
}

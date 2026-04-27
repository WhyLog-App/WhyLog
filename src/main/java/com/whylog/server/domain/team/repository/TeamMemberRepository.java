package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.entity.TeamMemberId;
import java.util.List;
import java.util.Optional;

import com.whylog.server.domain.team.enums.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    boolean existsByTeamIdAndMemberIdAndActiveTrue(Long teamId, Long memberId);

    @Query("""
            SELECT tm
            FROM TeamMember tm
            JOIN FETCH tm.team t
            WHERE tm.member.id = :memberId
              AND tm.active = true
            ORDER BY t.id ASC
            """)
    List<TeamMember> findActiveTeamsByMemberId(@Param("memberId") Long memberId);

    @Query("""
    SELECT tm
    FROM TeamMember tm
    JOIN FETCH tm.team
    WHERE tm.member.id = :memberId
      AND tm.team.id = :teamId
      AND tm.role = :role
""")
    Optional<TeamMember> findOwnerTeamMember(
            @Param("memberId") Long memberId,
            @Param("teamId") Long teamId,
            @Param("role") TeamRole role
    );

}

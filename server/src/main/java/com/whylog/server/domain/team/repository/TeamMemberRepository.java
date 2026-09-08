package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.team.entity.TeamMember;
import com.whylog.server.domain.team.entity.TeamMemberId;
import com.whylog.server.domain.team.enums.TeamRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    boolean existsByTeamIdAndMemberIdAndActiveTrue(Long teamId, Long memberId);

    @Query(
            """
            SELECT tm
            FROM TeamMember tm
            JOIN FETCH tm.team t
            WHERE tm.member.id = :memberId
              AND tm.active = true
            ORDER BY t.id ASC
            """)
    List<TeamMember> findActiveTeamsByMemberId(@Param("memberId") Long memberId);

    @Query(
            """
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
            @Param("role") TeamRole role);

    @Query(
            """
            SELECT tm.team.id
            FROM TeamMember tm
            WHERE tm.member.id = :memberId
              AND tm.role = :role
              AND tm.active = true
            ORDER BY tm.team.id ASC
            """)
    List<Long> findActiveOwnerTeamIdsByMemberId(
            @Param("memberId") Long memberId, @Param("role") TeamRole role);

    @Query(
            """
            SELECT tm.team.id
            FROM TeamMember tm
            WHERE tm.team.id IN :teamIds
              AND tm.active = true
            GROUP BY tm.team.id
            HAVING count(tm) > 1
            """)
    List<Long> findOwnedTeamIdsWithOtherActiveMembers(@Param("teamIds") List<Long> teamIds);

    @Modifying
    @Query(
            """
            UPDATE TeamMember tm
            SET tm.active = false
            WHERE tm.member.id = :memberId
              AND tm.active = true
            """)
    int deactivateActiveMembershipsByMemberId(@Param("memberId") Long memberId);

    @Query(
            """
            SELECT tm
            FROM TeamMember tm
            WHERE tm.team.id = :teamId
              AND tm.member.id IN :memberIds
            """)
    List<TeamMember> findByTeamIdAndMemberIdIn(
            @Param("teamId") Long teamId, @Param("memberIds") List<Long> memberIds);

    @Query(
            """
            SELECT count(tm)
            FROM TeamMember tm
            WHERE tm.member.id = :memberId
              AND tm.active = true
            """)
    long countActiveProjectsByMemberId(@Param("memberId") Long memberId);

    @Query(
            """
            SELECT new com.whylog.server.domain.team.repository.TeamMemberRepository$ActiveProjectRow(
              t.id,
              t.name,
              t.image
            )
            FROM TeamMember tm
            JOIN tm.team t
            WHERE tm.member.id = :memberId
              AND tm.active = true
              AND (:cursorId IS NULL OR t.id > :cursorId)
            ORDER BY t.id ASC
            """)
    Slice<ActiveProjectRow> findActiveProjectRowsByMemberId(
            @Param("memberId") Long memberId, @Param("cursorId") Long cursorId, Pageable pageable);

    record ActiveProjectRow(Long projectId, String name, String image) {}
}

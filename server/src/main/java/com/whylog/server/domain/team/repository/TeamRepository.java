package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.entity.Team;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Boolean existsByName(String teamName);

    @Query(
            """
      SELECT new com.whylog.server.domain.decision.dto.DecisionResponse$DecisionFlatRow(
        d.id,
        m.name,
        a.id,
        a.name
      )
      FROM Decision d
      JOIN d.meeting m
      LEFT JOIN d.applications a
      WHERE m.team.id = :teamId
      ORDER BY d.createdAt desc
  """)
    List<DecisionResponse.DecisionFlatRow> findDecisionRows(@Param("teamId") Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            SELECT t
            FROM Team t
            WHERE t.id IN :teamIds
            ORDER BY t.id ASC
            """)
    List<Team> findAllByIdInForUpdate(@Param("teamIds") List<Long> teamIds);
}

package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Boolean existsByName(String teamName);

    @Query("""
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
      ORDER BY d.id
  """)
    List<DecisionResponse.DecisionFlatRow> findDecisionRows(@Param("teamId") Long teamId);

}

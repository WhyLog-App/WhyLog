package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Boolean existsByName(String teamName);

    @Query("""
      SELECT DISTINCT d
      FROM Decision d
      JOIN FETCH d.meeting m
      LEFT JOIN FETCH d.applications
      WHERE m.team.id = :teamId
  """)
    List<Decision> findDecisions(@Param("teamId") Long teamId);

}

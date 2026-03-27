package com.whylog.server.domain.team.repository;

import com.whylog.server.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Boolean existsByName(String teamName);

}

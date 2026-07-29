package com.whylog.server.domain.team.service;

import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.exception.TeamNotFoundException;
import com.whylog.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamUseCase {

    private final TeamRepository teamRepository;

    public Team findTeamById(Long id){
        return teamRepository.findById(id)
                .orElseThrow(TeamNotFoundException::new);
    }

}

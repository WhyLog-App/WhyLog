package com.whylog.server.domain.team.service;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamQueryService {

    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<DecisionResponse.DecisionListDTO> decisions( Long teamId ){

        List<Decision> decisions = teamRepository.findDecisions(teamId);

        return decisions.stream()
                .map(d -> DecisionResponse.DecisionListDTO.builder()
                        .decisionId(d.getId())
                        .name(d.getMeeting().getName())
                        .applications(
                                d.getApplications().stream()
                                        .map(application -> ApplicationResponse.ApplicationDTO.builder()
                                                .applicationId(application.getId())
                                                .name(application.getName())
                                                .build())
                                        .toList()
                        )
                        .build()
                ).toList();
    }

}

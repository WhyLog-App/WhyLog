package com.whylog.server.domain.team.service;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamQueryService {

    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<DecisionResponse.DecisionListDTO> decisions( Long teamId ){

        List<DecisionResponse.DecisionFlatRow> rows = teamRepository.findDecisionRows(teamId);
        Map<Long, DecisionAccumulator> decisions = new LinkedHashMap<>();

        for (DecisionResponse.DecisionFlatRow row : rows) {
            DecisionAccumulator decision = decisions.computeIfAbsent(
                    row.decisionId(),
                    decisionId -> new DecisionAccumulator(decisionId, row.meetingName())
            );

            if (row.applicationId() != null) {
                decision.applications().add(ApplicationResponse.ApplicationDTO.builder()
                        .applicationId(row.applicationId())
                        .name(row.applicationName())
                        .build());
            }
        }

        return decisions.values().stream()
                .map(decision -> DecisionResponse.DecisionListDTO.builder()
                        .decisionId(decision.decisionId())
                        .name(decision.meetingName())
                        .applications(decision.applications())
                        .build())
                .toList();
    }

    private record DecisionAccumulator(
            Long decisionId,
            String meetingName,
            List<ApplicationResponse.ApplicationDTO> applications
    ) {
        private DecisionAccumulator(Long decisionId, String meetingName) {
            this(decisionId, meetingName, new ArrayList<>());
        }
    }

}

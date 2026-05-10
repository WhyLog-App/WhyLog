package com.whylog.server.domain.decision.service;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.exception.DecisionNotFoundException;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DecisionQueryService {

    private final DecisionRepository decisionRepository;

    // 결정사항의 신뢰도 조회
    public DecisionResponse.ReliabilityDTO getReliability(Long decisionId) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(DecisionNotFoundException::new);

        return DecisionResponse.ReliabilityDTO.builder()
                .score(decision.getReliabilityScore())
                .build();
    }
}

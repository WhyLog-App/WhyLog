package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionBaseRepository extends JpaRepository<DecisionBase, Long> {
}

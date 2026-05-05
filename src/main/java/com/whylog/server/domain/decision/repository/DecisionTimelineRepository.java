package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.DecisionTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionTimelineRepository extends JpaRepository<DecisionTimeline, Long> {
}

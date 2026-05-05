package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.entity.ApplicationTimelineId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTimelineRepository extends JpaRepository<ApplicationTimeline, ApplicationTimelineId> {
}

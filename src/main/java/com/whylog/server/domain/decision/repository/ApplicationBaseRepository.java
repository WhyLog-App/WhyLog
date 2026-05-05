package com.whylog.server.domain.decision.repository;

import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationBaseId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationBaseRepository extends JpaRepository<ApplicationBase, ApplicationBaseId> {
}

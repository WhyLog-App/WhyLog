package com.whylog.server.domain.decision.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Application_Commits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationCommits extends BaseEntity {

    @EmbeddedId
    private ApplicationCommitsId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionCommitsPk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_commits_pk", nullable = false)
    private DecisionCommits decisionCommits;

    public static ApplicationCommits create(Application application, DecisionCommits decisionCommits) {
        ApplicationCommits applicationCommits = new ApplicationCommits();
        applicationCommits.id = new ApplicationCommitsId(application.getId(), decisionCommits.getId());
        applicationCommits.application = application;
        applicationCommits.decisionCommits = decisionCommits;
        return applicationCommits;
    }
}

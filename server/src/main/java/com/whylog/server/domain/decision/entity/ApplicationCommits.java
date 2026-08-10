package com.whylog.server.domain.decision.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "application_commits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationCommits extends BaseEntity {

    @EmbeddedId private ApplicationCommitsId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionCommitsPk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_commits_pk", nullable = false)
    private DecisionCommits decisionCommits;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "confidence")
    private Integer confidence;

    public static ApplicationCommits create(
            Application application,
            DecisionCommits decisionCommits,
            String reason,
            Integer confidence) {
        ApplicationCommits applicationCommits = new ApplicationCommits();
        applicationCommits.id =
                new ApplicationCommitsId(application.getId(), decisionCommits.getId());
        applicationCommits.application = application;
        applicationCommits.decisionCommits = decisionCommits;
        applicationCommits.reason = reason;
        applicationCommits.confidence = confidence;
        return applicationCommits;
    }
}

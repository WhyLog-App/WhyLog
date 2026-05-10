package com.whylog.server.domain.decision.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "Decision_Commits",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_decision_commits_decision_commit",
                columnNames = {"decision_id", "commit_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecisionCommits extends BaseEntity {

    @Id
    @Column(name = "decision_commits_pk")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(name = "commit_id", nullable = false)
    private Long commitId; // 매핑 X,

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    public static DecisionCommits create(Decision decision, Long commitId, String reason) {
        DecisionCommits decisionCommits = new DecisionCommits();
        decisionCommits.decision = decision;
        decisionCommits.commitId = commitId;
        decisionCommits.reason = reason;
        return decisionCommits;
    }

    // 저장시
    public void updateReason(String reason) {
        this.reason = reason;
    }
}

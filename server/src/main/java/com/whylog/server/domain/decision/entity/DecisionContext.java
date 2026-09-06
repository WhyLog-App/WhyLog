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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "decision_context")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecisionContext extends BaseEntity {

    @Id
    @Column(name = "decision_context_pk")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(name = "timestamp", length = 30)
    private String timestamp;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "utterance", columnDefinition = "TEXT")
    private String utterance;

    public static DecisionContext create(
            Decision decision, String timestamp, String content, Long memberId, String utterance) {
        DecisionContext decisionContext = new DecisionContext();
        decisionContext.decision = decision;
        decisionContext.timestamp = timestamp;
        decisionContext.content = content;
        decisionContext.memberId = memberId;
        decisionContext.utterance = utterance;
        return decisionContext;
    }
}

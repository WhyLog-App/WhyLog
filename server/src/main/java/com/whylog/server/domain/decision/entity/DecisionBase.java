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

// 결정근거
@Entity
@Getter
@Table(name = "decision_base")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecisionBase extends BaseEntity {

    @Id
    @Column(name = "decision_base_pk")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public static DecisionBase create(Decision decision, String content) {
        DecisionBase decisionBase = new DecisionBase();
        decisionBase.decision = decision;
        decisionBase.content = content;
        return decisionBase;
    }
}

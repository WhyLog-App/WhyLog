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
@Table(name = "decision_timeline")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecisionTimeline extends BaseEntity {

    @Id
    @Column(name = "decision_timeline_pk")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(name = "timestamp", length = 30)
    private String timestamp;

    // 타임라인 단계
    @Column(name = "step", length = 10)
    private String step;

    // 타임라인 내용 요약
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 발화자
    @Column(name = "member_id")
    private Long memberId;

    // 발화 원문
    @Column(name = "utterance", columnDefinition = "TEXT")
    private String utterance;

    public static DecisionTimeline create(
            Decision decision,
            String timestamp,
            String step,
            String content,
            Long memberId,
            String utterance) {
        DecisionTimeline decisionTimeline = new DecisionTimeline();
        decisionTimeline.decision = decision;
        decisionTimeline.timestamp = timestamp;
        decisionTimeline.step = step;
        decisionTimeline.content = content;
        decisionTimeline.memberId = memberId;
        decisionTimeline.utterance = utterance;
        return decisionTimeline;
    }
}

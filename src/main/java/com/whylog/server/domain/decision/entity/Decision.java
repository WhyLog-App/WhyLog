package com.whylog.server.domain.decision.entity;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Decision")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Decision extends BaseEntity {

    @Id
    @Column(name = "decision_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true)
    private Meeting meeting;

    @Column(name = "is_created")
    private Boolean isCreated;

    @Column(name = "reliability_score")
    private Integer reliabilityScore;

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Application> applications = new ArrayList<>();
//
//    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<DecisionTimeline> decisionTimelines = new ArrayList<>();
//
//    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
//    private final List<DecisionBase> decisionBases = new ArrayList<>();

    public static Decision create(Meeting meeting, boolean isCreated) {
        Decision decision = new Decision();
        decision.meeting = meeting;
        decision.isCreated = isCreated;
        return decision;
    }

    public void updateReliabilityScore(Integer reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }
}

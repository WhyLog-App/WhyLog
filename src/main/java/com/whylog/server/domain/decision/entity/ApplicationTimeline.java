package com.whylog.server.domain.decision.entity;

import com.whylog.server.global.entity.BaseEntity;
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
@Table(name = "Application_Timeline")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationTimeline extends BaseEntity {

    @EmbeddedId
    private ApplicationTimelineId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionTimelinePk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_timeline_pk", nullable = false)
    private DecisionTimeline decisionTimeline;

    public static ApplicationTimeline create(Application application, DecisionTimeline decisionTimeline) {
        ApplicationTimeline applicationTimeline = new ApplicationTimeline();
        applicationTimeline.id = new ApplicationTimelineId(application.getId(), decisionTimeline.getId());
        applicationTimeline.application = application;
        applicationTimeline.decisionTimeline = decisionTimeline;
        return applicationTimeline;
    }
}

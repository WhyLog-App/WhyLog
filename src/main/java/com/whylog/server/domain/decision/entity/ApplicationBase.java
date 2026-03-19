package com.whylog.server.domain.decision.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Application_Base")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationBase extends BaseEntity {

    @EmbeddedId
    private ApplicationBaseId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionBasePk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_base_pk", nullable = false)
    private DecisionBase decisionBase;
}

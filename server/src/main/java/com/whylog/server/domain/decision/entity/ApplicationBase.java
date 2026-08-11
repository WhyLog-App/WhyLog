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
@Table(name = "application_base")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationBase extends BaseEntity {

    @EmbeddedId private ApplicationBaseId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionBasePk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_base_pk", nullable = false)
    private DecisionBase decisionBase;

    public static ApplicationBase create(Application application, DecisionBase decisionBase) {
        ApplicationBase applicationBase = new ApplicationBase();
        applicationBase.id = new ApplicationBaseId(application.getId(), decisionBase.getId());
        applicationBase.application = application;
        applicationBase.decisionBase = decisionBase;
        return applicationBase;
    }
}

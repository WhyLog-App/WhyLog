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
@Table(name = "application_context")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationContext extends BaseEntity {

    @EmbeddedId private ApplicationContextId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @MapsId("decisionContextPk")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_context_pk", nullable = false)
    private DecisionContext decisionContext;

    public static ApplicationContext create(
            Application application, DecisionContext decisionContext) {
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.id =
                new ApplicationContextId(application.getId(), decisionContext.getId());
        applicationContext.application = application;
        applicationContext.decisionContext = decisionContext;
        return applicationContext;
    }
}

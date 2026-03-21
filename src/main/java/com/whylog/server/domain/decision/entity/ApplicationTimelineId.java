package com.whylog.server.domain.decision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationTimelineId implements Serializable {

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "decision_timeline_pk")
    private Long decisionTimelinePk;
}

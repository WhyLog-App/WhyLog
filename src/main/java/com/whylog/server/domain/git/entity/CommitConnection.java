package com.whylog.server.domain.git.entity;

import com.whylog.server.domain.decision.entity.Decision;
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
@Table(name = "Commit_Connection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommitConnection extends BaseEntity {

    @EmbeddedId
    private CommitConnectionId id;

    @MapsId("decisionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @MapsId("commitId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false)
    private Commit commit;
}

package com.whylog.server.domain.git.entity;

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
public class CommitConnectionId implements Serializable {

    @Column(name = "decision_id")
    private Long decisionId;

    @Column(name = "commit_id")
    private Long commitId;
}

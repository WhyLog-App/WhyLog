package com.whylog.server.domain.git.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Commit_Analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommitAnalysis extends BaseEntity {

    @Id
    @Column(name = "commit_Analysis_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false, unique = true)
    private Commit commit;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    public static CommitAnalysis create(Commit commit) {
        CommitAnalysis commitAnalysis = new CommitAnalysis();
        commitAnalysis.commit = commit;
        return commitAnalysis;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}

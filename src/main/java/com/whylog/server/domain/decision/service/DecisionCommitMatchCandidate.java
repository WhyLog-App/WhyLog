package com.whylog.server.domain.decision.service;

final class DecisionCommitMatchCandidate {

    private final Long applicationId;
    private final String applicationTitle;
    private final Long commitId;
    private final Long repositoryId;
    private final String commitHash;
    private final String reason;
    private Long resolvedApplicationId;
    private Long resolvedCommitId;

    DecisionCommitMatchCandidate(Long applicationId,
                                 String applicationTitle,
                                 Long commitId,
                                 Long repositoryId,
                                 String commitHash,
                                 String reason) {
        this.applicationId = applicationId;
        this.applicationTitle = applicationTitle;
        this.commitId = commitId;
        this.repositoryId = repositoryId;
        this.commitHash = commitHash;
        this.reason = reason;
    }

    Long applicationId() {
        return applicationId;
    }

    String applicationTitle() {
        return applicationTitle;
    }

    Long commitId() {
        return commitId;
    }

    Long repositoryId() {
        return repositoryId;
    }

    String commitHash() {
        return commitHash;
    }

    String reason() {
        return reason;
    }

    void resolveApplicationId(Long resolvedApplicationId) {
        this.resolvedApplicationId = resolvedApplicationId;
    }

    Long resolvedApplicationId() {
        return resolvedApplicationId;
    }

    void resolveCommitId(Long resolvedCommitId) {
        this.resolvedCommitId = resolvedCommitId;
    }

    Long resolvedCommitId() {
        return resolvedCommitId;
    }

    String commitUniqueKey() {
        if (commitId != null) {
            return "id:" + commitId;
        }
        return "repo:" + repositoryId + ":hash:" + commitHash;
    }

    String applicationUniqueKey() {
        if (applicationId != null) {
            return "id:" + applicationId;
        }
        return "title:" + applicationTitle;
    }
}

package com.whylog.server.domain.user.enums;

public enum AccountStatus {
    UNVERIFIED,
    ACTIVE,
    INACTIVE,
    WITHDRAW;

    public boolean canUseNormalService() {
        return this == ACTIVE;
    }
}

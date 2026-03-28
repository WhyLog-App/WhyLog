package com.whylog.server.domain.team.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class TeamNotFoundException extends GeneralException {

    public TeamNotFoundException() {
        super(TeamErrorCode.TEAM_NOT_FOUND);
    }

}

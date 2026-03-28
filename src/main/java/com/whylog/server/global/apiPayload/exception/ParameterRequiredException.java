package com.whylog.server.global.apiPayload.exception;

import com.whylog.server.global.apiPayload.code.status.ErrorStatus;

public class ParameterRequiredException extends GeneralException {
  public ParameterRequiredException() {
    super(ErrorStatus._PARAMETER_REQUIRED);
  }
}

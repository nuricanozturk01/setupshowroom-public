package com.setupshowroom.shared.exceptionhandler.exception;

import jakarta.validation.constraints.NotNull;

public class UsernameAlreadyExistsException extends BaseException {

  public UsernameAlreadyExistsException(final @NotNull String msgId) {
    super(msgId);
  }
}

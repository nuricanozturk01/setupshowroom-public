package com.setupshowroom.shared.exceptionhandler.exception;

import org.jetbrains.annotations.NotNull;

public class EmailCannotChangeException extends BaseException {
  public EmailCannotChangeException(final @NotNull String msgId) {
    super(msgId);
  }
}

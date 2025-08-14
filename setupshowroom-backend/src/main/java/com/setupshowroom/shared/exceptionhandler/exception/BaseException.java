package com.setupshowroom.shared.exceptionhandler.exception;

import jakarta.validation.constraints.NotNull;

class BaseException extends RuntimeException {
  BaseException(final @NotNull String msgId) {
    super(msgId);
  }

  BaseException(final @NotNull String message, final Throwable cause) {
    super(message, cause);
  }
}

package com.setupshowroom.shared.exceptionhandler.exception;

import org.jetbrains.annotations.NotNull;

public class TokenExpiredException extends RuntimeException {
  public TokenExpiredException(final @NotNull String message) {
    super(message);
  }
}

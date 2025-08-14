package com.setupshowroom.shared.exceptionhandler.exception;

import org.jetbrains.annotations.NotNull;

public class UnPermittedContentException extends RuntimeException {
  public UnPermittedContentException(final @NotNull String message) {
    super(message);
  }
}

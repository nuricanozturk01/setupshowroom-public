package com.setupshowroom.shared.exceptionhandler.exception;

import org.jetbrains.annotations.NotNull;

public class ItemNotFoundException extends RuntimeException {
  public ItemNotFoundException(final @NotNull String message) {
    super(message);
  }
}

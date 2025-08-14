package com.setupshowroom.shared.exceptionhandler.exception;

public class ModerationFailedException extends RuntimeException {
  public ModerationFailedException(final String filename, final String reason) {
    super("Content [" + filename + "] rejected due to: " + reason);
  }
}

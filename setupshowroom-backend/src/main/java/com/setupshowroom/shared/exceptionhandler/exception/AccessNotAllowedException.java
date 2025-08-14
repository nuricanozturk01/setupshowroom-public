package com.setupshowroom.shared.exceptionhandler.exception;

public class AccessNotAllowedException extends RuntimeException {
  public AccessNotAllowedException(final String message) {
    super(message);
  }
}

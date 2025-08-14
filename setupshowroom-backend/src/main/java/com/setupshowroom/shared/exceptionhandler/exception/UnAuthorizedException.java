package com.setupshowroom.shared.exceptionhandler.exception;

import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class UnAuthorizedException extends BaseException {
  private @Nullable Map<String, String> headers;

  public UnAuthorizedException(
      final @NotNull String msgId, final @Nullable Map<String, String> headers) {
    super(msgId);
    this.headers = headers;
  }

  public UnAuthorizedException(final @NotNull String msgId) {
    super(msgId);
  }
}

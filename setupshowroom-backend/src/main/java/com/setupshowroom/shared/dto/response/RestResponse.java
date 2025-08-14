package com.setupshowroom.shared.dto.response;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RestResponse<T> {
  private final @NotNull String msgId;
  @Getter private final @NotNull ResponseType type;
  @Getter private T data;
  @Getter private String errorCode;
  @Getter private String text;

  public RestResponse(final @NotNull String msgId, final @NotNull ResponseType type) {
    this.msgId = msgId;
    this.type = type;
  }

  public void setData(final @Nullable T data) {
    this.data = data;
  }

  public void setErrorCode(final @NotNull String errorCode) {
    this.errorCode = errorCode;
  }

  public void setText(final @NotNull String text) {
    this.text = text;
  }

  public @NotNull String getMsgId() {
    return this.msgId;
  }
}

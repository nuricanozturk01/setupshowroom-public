package com.setupshowroom.shared.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Response<T> {
  private final String message;
  private final T data;
  private final boolean success;
  private final int status;

  public static <T> @NotNull Response<T> success(
      final @NotNull String message, final @NotNull T data, final int status) {
    return new Response<>(message, data, true, status);
  }

  public static <T> @NotNull Response<T> success(final @NotNull String message) {
    return new Response<>(message, null, true, HttpStatus.OK.value());
  }

  public static <T> @NotNull Response<T> error(final @NotNull String message, final int status) {
    return new Response<>(message, null, false, status);
  }
}

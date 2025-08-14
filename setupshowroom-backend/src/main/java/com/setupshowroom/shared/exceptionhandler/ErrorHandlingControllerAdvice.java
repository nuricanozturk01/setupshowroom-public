package com.setupshowroom.shared.exceptionhandler;

import com.setupshowroom.shared.dto.response.ResponseType;
import com.setupshowroom.shared.dto.response.RestResponse;
import com.setupshowroom.shared.exceptionhandler.exception.AccessNotAllowedException;
import com.setupshowroom.shared.exceptionhandler.exception.EmailCannotChangeException;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.shared.exceptionhandler.exception.UnAuthorizedException;
import com.setupshowroom.shared.exceptionhandler.exception.UnPermittedContentException;
import com.setupshowroom.shared.exceptionhandler.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorHandlingControllerAdvice {
  private static final Set<String> NOT_LOGGED_EXCEPTIONS =
      Set.of(
          "org.springframework.web.servlet.resource.NoResourceFoundException",
          "org.apache.catalina.connector.ClientAbortException",
          "setupshowroom.com.common.exceptionhandler.exception.TokenExpiredException",
          "org.springframework.web.context.request.async.AsyncRequestTimeoutException",
          "java.nio.channels.ClosedChannelException",
          "org.springframework.web.context.request.async.AsyncRequestNotUsableException");

  private final MessageSource messageSource;

  @ExceptionHandler(Throwable.class)
  @Nullable
  ResponseEntity<RestResponse<Object>> defaultExceptionHandler(
      final @NotNull Throwable ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    if (ex instanceof final HttpClientErrorException exception) {
      log.error(exception.getResponseBodyAsString());
    }

    if (!NOT_LOGGED_EXCEPTIONS.contains(ex.getClass().getName())) {
      log.error(ex.getMessage(), ex);
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "errorOccured", null));
  }

  @ExceptionHandler(AccessNotAllowedException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull AccessNotAllowedException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    final String exceptionMessage = ex.getMessage();
    final String messageText = exceptionMessage != null ? exceptionMessage : "accessNotAllowed";

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, messageText, ex.getMessage()));
  }

  @ExceptionHandler(UnPermittedContentException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull UnPermittedContentException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    final String exceptionMessage = ex.getMessage();
    final String messageText = exceptionMessage != null ? exceptionMessage : "dangerousContent";

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, messageText, ex.getMessage()));
  }

  @ExceptionHandler(ConversionFailedException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull ConversionFailedException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "itemNotFound", ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull HttpMessageNotReadableException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "badRequest", ex.getMessage()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @Nullable
  ResponseEntity<RestResponse<Void>> handleException(
      final @NotNull HttpRequestMethodNotSupportedException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "methodNotSupported", null));
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  @Nullable
  ResponseEntity<RestResponse<Void>> handleException(
      final @NotNull HttpMediaTypeNotAcceptableException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "methodNotSupported", null));
  }

  @ExceptionHandler(ItemNotFoundException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull ItemNotFoundException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    final String exceptionMessage = ex.getMessage();
    final String messageText = exceptionMessage != null ? exceptionMessage : "itemNotFound";

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, messageText, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @Nullable
  ResponseEntity<RestResponse<Void>> handleException(
      final @NotNull MethodArgumentNotValidException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "validationError", null));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull MissingServletRequestParameterException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "validationError", ex.getParameterName()));
  }

  @ExceptionHandler(UnAuthorizedException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull UnAuthorizedException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    if (ex.getHeaders() != null) {
      ex.getHeaders().forEach(response::addHeader);
    }

    log.info(ex.getMessage());

    final String exceptionMessage = ex.getMessage();
    final String messageText = exceptionMessage != null ? exceptionMessage : "unauthorizedRequest";

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, messageText, ex.getMessage()));
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull UsernameAlreadyExistsException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "usernameAlreadyExists", ex.getMessage()));
  }

  @ExceptionHandler(EmailCannotChangeException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull EmailCannotChangeException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "emailCannotChange", ex.getMessage()));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull HttpMediaTypeNotSupportedException ex,
      final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "validationError", null));
  }

  /**
   * Returns form validation errors from controller validations
   *
   * @param ex Thrown validation exception
   * @return REST response
   */
  @ExceptionHandler(ValidationException.class)
  @Nullable
  ResponseEntity<RestResponse<String>> handleException(
      final @NotNull ValidationException ex, final @Nullable HttpServletResponse response) {
    if (response == null) {
      return null;
    }

    log.info(ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(this.createMessage(ResponseType.ERROR, "validationError", ex.getMessage()));
  }

  @SuppressWarnings("all")
  private @NotNull <T> RestResponse<T> createMessage(
      final @NotNull ResponseType type, final @NotNull String msgId, final @Nullable T data) {
    final RestResponse<T> restResponse = new RestResponse<>(msgId, type);

    restResponse.setText(
        Objects.requireNonNull(
            this.messageSource.getMessage(msgId, null, msgId, Locale.getDefault())));
    restResponse.setData(data);

    return restResponse;
  }
}

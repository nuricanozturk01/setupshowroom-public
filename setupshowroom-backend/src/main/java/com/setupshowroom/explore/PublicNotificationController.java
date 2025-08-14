package com.setupshowroom.explore;

import com.setupshowroom.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/public/notification")
@RequiredArgsConstructor
@CrossOrigin(
    origins = "http://localhost:4200",
    allowedHeaders = "*",
    exposedHeaders = "*",
    allowCredentials = "false")
public class PublicNotificationController {
  private static final String X_ACCEL_BUFFERING = "X-Accel-Buffering";
  private final @NotNull NotificationService notificationService;

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> streamNotifications(
      @RequestParam final @NotNull String userId) {
    final SseEmitter emitter = this.notificationService.subscribe(userId);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .header(HttpHeaders.CONNECTION, "keep-alive")
        .header(X_ACCEL_BUFFERING, "no")
        .body(emitter);
  }
}

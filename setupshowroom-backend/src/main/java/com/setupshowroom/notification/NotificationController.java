package com.setupshowroom.notification;

import com.setupshowroom.notification.dto.NotificationInfo;
import com.setupshowroom.shared.dto.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/user/{userId}/notification")
@RestController
@RequiredArgsConstructor
public class NotificationController {
  private final @NotNull NotificationService notificationService;

  @PostMapping("/read-all")
  public @NotNull ResponseEntity<Response<?>> markAsReadAll(
      @PathVariable final @NotNull String userId) {
    this.notificationService.markAsReadAll(userId);
    final var response = Response.success("retrieved", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public @NotNull ResponseEntity<Response<List<NotificationInfo>>> getAllNotificationsReadFalse(
      @PathVariable final @NotNull String userId, @PageableDefault final Pageable pageable) {
    final var response =
        Response.success(
            "retrieved",
            this.notificationService.findAllByUserIdAndReadFalse(userId, pageable),
            HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/all")
  public @NotNull ResponseEntity<Response<List<NotificationInfo>>> getAll(
      @PathVariable final @NotNull String userId, @PageableDefault final Pageable pageable) {
    final var notifications = this.notificationService.findAll(userId, pageable);
    final var response = Response.success("retrieved", notifications, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/read")
  public @NotNull ResponseEntity<?> getAllNotificationsReadTrue(
      @PathVariable final @NotNull String userId, final @PageableDefault Pageable pageable) {
    return ResponseEntity.ok(this.notificationService.findAllByUserIdAndReadTrue(userId, pageable));
  }

  @PostMapping("/{notificationId}/read")
  public @NotNull ResponseEntity<Response<?>> markAsRead(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String notificationId) {
    this.notificationService.markAsRead(notificationId, userId);
    final var response = Response.success("marked as read", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{notificationId}/unread")
  public @NotNull ResponseEntity<Response<?>> markAsUnread(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String notificationId) {
    this.notificationService.markAsUnread(notificationId, userId);
    final var response = Response.success("marked as unread", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{notificationId}/delete")
  public @NotNull ResponseEntity<Response<?>> deleteNotification(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String notificationId) {
    this.notificationService.deleteNotification(notificationId, userId);
    final var response = Response.success("deleted", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/delete")
  public @NotNull ResponseEntity<Response<?>> deleteAllNotifications(
      @PathVariable final @NotNull String userId) {
    this.notificationService.deleteAllNotifications(userId);
    final var response = Response.success("deleted", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }
}

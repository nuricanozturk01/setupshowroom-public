package com.setupshowroom.notification;

import com.setupshowroom.notification.converter.NotificationConverter;
import com.setupshowroom.notification.dto.NotificationForm;
import com.setupshowroom.notification.dto.NotificationInfo;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import com.setupshowroom.user.converter.UserConverter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
  private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
  private static final Duration EMITTER_TIMEOUT = Duration.ofHours(24);

  private final @NotNull NotificationRepository notificationRepository;
  private final @NotNull NotificationConverter notificationConverter;
  private final @NotNull UserRepository userRepository;
  private final @NotNull UserConverter userConverter;
  private final TaskScheduler taskScheduler;
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    this.taskScheduler.scheduleAtFixedRate(this::sendHeartbeatToAll, HEARTBEAT_INTERVAL);
  }

  private void sendHeartbeatToAll() {
    this.emitters.forEach(
        (userId, emitter) -> {
          try {
            emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            log.debug("Heartbeat sent to user: {}", userId);
          } catch (final IOException e) {
            log.info("Failed to send heartbeat to user: {}, removing emitter", userId);
            this.removeEmitter(userId);
          }
        });
  }

  public SseEmitter subscribe(final @NotNull String userId) {
    log.info("User {} attempting to subscribe to notifications", userId);
    final SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT.toMillis());

    emitter.onTimeout(
        () -> {
          log.info("SSE connection timeout for user: {}", userId);
          this.removeEmitter(userId);
        });

    emitter.onCompletion(
        () -> {
          log.info("SSE connection completed for user: {}", userId);
          this.removeEmitter(userId);
        });

    emitter.onError(
        ex -> {
          log.info("SSE connection error for user: {}", userId, ex);
          this.removeEmitter(userId);
        });

    this.emitters.put(userId, emitter);
    log.info("User {} successfully subscribed to notifications", userId);

    try {
      emitter.send(SseEmitter.event().name("INIT").data("Connected!"));
    } catch (final IOException e) {
      log.error("Error sending initial message to user: {}", userId, e);
      this.removeEmitter(userId);
    }

    return emitter;
  }

  @SuppressWarnings("all")
  public @NotNull Notification createNotification(
      final @NotNull NotificationForm notificationForm, final @NotNull String userId) {
    final User user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    final Notification notification = this.notificationConverter.toNotification(notificationForm);

    notification.setUser(user);

    final Notification savedNotification = this.notificationRepository.save(notification);

    final var toUser =
        this.userRepository
            .findUserById(notificationForm.getTo())
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    final var notificationInfo =
        NotificationInfo.builder()
            .id(savedNotification.getId())
            .title(savedNotification.getTitle())
            .description(savedNotification.getDescription())
            .type(savedNotification.getType())
            .action(savedNotification.getAction())
            .user(this.userConverter.toUserInfo(toUser))
            .read(savedNotification.isRead())
            .createdAt(Instant.now())
            .build();

    final SseEmitter emitter = emitters.get(notificationForm.getTo());
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(notificationInfo));
      } catch (final @NotNull IOException e) {
        log.warn(
            "Error sending notification to user: {} - Error: {}",
            notificationForm.getTo(),
            e.getMessage());
        removeEmitter(notificationForm.getTo());
      }
    }

    return savedNotification;
  }

  @PreDestroy
  public void destroy() {
    log.info("Shutting down notification service, closing all SSE connections...");
    this.emitters.forEach(
        (userId, emitter) -> {
          try {
            emitter.send(SseEmitter.event().name("shutdown").data("Server shutting down"));
            this.removeEmitter(userId);
          } catch (final IOException e) {
            log.warn("Error during shutdown notification for user: {}", userId);
          }
        });
  }

  public @NotNull List<NotificationInfo> findAllByUserIdAndReadTrue(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    return this.notificationRepository.findAllByUserIdAndReadTrueAndDeletedFalse(userId, pageable);
  }

  public @NotNull List<NotificationInfo> findAllByUserIdAndReadFalse(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    return this.notificationRepository.findAllByUserIdAndReadFalseAndDeletedFalse(userId, pageable);
  }

  public void markAsRead(final @NotNull String notificationId, final @NotNull String userId) {
    this.notificationRepository.markAsRead(notificationId, userId);
  }

  public void markAsUnread(final @NotNull String notificationId, final @NotNull String userId) {
    this.notificationRepository.markAsUnread(notificationId, userId);
  }

  public void deleteNotification(
      final @NotNull String notificationId, final @NotNull String userId) {
    this.notificationRepository.deleteNotification(notificationId, userId);
  }

  public void deleteAllNotifications(final @NotNull String userId) {
    this.notificationRepository.deleteAllNotifications(userId);
  }

  public List<NotificationInfo> findAll(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    return this.notificationRepository.findAllByUserIdAndDeletedFalse(userId, pageable);
  }

  public void markAsReadAll(final @NotNull String userId) {
    this.notificationRepository.markAsReadAll(userId);
  }

  private void removeEmitter(final @NotNull String userId) {
    final SseEmitter emitter = this.emitters.remove(userId);
    if (emitter != null) {
      emitter.complete();
    }
  }
}

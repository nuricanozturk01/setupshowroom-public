package com.setupshowroom.setup;

import static com.setupshowroom.shared.storage.SetupStorageService.IMAGE_PATH;
import static com.setupshowroom.shared.storage.SetupStorageService.VIDEO_PATH;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.comment.dto.CommentForm;
import com.setupshowroom.comment.dto.CommentInfo;
import com.setupshowroom.notification.NotificationService;
import com.setupshowroom.notification.NotificationType;
import com.setupshowroom.notification.dto.NotificationForm;
import com.setupshowroom.setup.dto.ProfileSetupForm;
import com.setupshowroom.setup.dto.SetupForm;
import com.setupshowroom.setup.dto.SetupInfo;
import com.setupshowroom.setup.dto.SetupUpdateForm;
import com.setupshowroom.shared.contentmoderation.ContentModerationService;
import com.setupshowroom.shared.storage.SetupStorageService;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetupFacade {
  private final @NotNull SetupService setupService;
  private final @NotNull SetupStorageService setupStorageService;
  private final @NotNull UserRepository userRepository;
  private final @NotNull NotificationService notificationService;
  private final @NotNull ContentModerationService contentModerationService;

  public @NotNull SetupInfo createSetup(
      final @NotNull String userId,
      final @NotNull SetupForm setupForm,
      final @NotNull List<MultipartFile> images,
      final @NotNull List<MultipartFile> videos) {
    final User user = this.findUserById(userId);
    setupForm.setSetupId(UlidCreator.getUlid().toString());

    this.contentModerationService.checkContentValidity(user, images, videos);

    final List<String> videoUrls =
        this.setupStorageService.writeContents(
            user.getId(), setupForm.getSetupId(), videos, VIDEO_PATH);

    final List<String> imageUrls =
        this.setupStorageService.writeContents(
            user.getId(), setupForm.getSetupId(), images, IMAGE_PATH);

    log.warn("Setup created for user {}", userId);

    return this.setupService.createSetup(user, setupForm, imageUrls, videoUrls);
  }

  public @NotNull List<SetupInfo> findAllSetupsByUser(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    final User user = this.findUserById(userId);

    return this.setupService.findAllSetupsByUser(user, pageable);
  }

  public @NotNull SetupInfo findSetupById(
      final @NotNull String userId, final @NotNull String setupId) {
    final User user = this.findUserById(userId);

    return this.setupService.findSetupById(user, setupId);
  }

  public void addToFavorite(final @NotNull String setupId, final @NotNull String userId) {
    final User user = this.findUserById(userId);

    this.setupService.addToFavorite(setupId, user);
  }

  public void removeFromFavorite(final @NotNull String setupId, final @NotNull String userId) {
    final User user = this.findUserById(userId);

    this.setupService.removeFromFavorite(setupId, user);
  }

  public void deleteSetupById(final @NotNull String setupId, final @NotNull String userId) {
    final User user = this.findUserById(userId);

    this.setupService.deleteSetupById(user, setupId);

    this.setupStorageService.deleteSetup(userId, setupId);
  }

  public @NotNull SetupInfo updateSetup(
      final @NotNull String setupId,
      final @NotNull String userId,
      final @NotNull SetupUpdateForm setupForm,
      final @NotNull List<MultipartFile> images,
      final @NotNull List<MultipartFile> videos) {
    final User user = this.findUserById(userId);

    final SetupInfo setupInfo = this.setupService.findSetupById(user, setupId);

    this.contentModerationService.checkContentValidity(user, images, videos);

    final SortedSet<String> newImageUrls =
        new TreeSet<>(
            this.setupStorageService.writeContents(user.getId(), setupId, images, IMAGE_PATH));
    final SortedSet<String> newVideoUrls =
        new TreeSet<>(
            this.setupStorageService.writeContents(user.getId(), setupId, videos, VIDEO_PATH));

    final SortedSet<String> finalImages =
        Stream.concat(setupForm.getExistingImages().stream(), newImageUrls.stream())
            .collect(Collectors.toCollection(TreeSet::new));

    final SortedSet<String> finalVideos =
        Stream.concat(setupForm.getExistingVideos().stream(), newVideoUrls.stream())
            .collect(Collectors.toCollection(TreeSet::new));

    // Delete unused images and videos
    final SortedSet<String> removedImages = new TreeSet<>(setupInfo.getImages());
    removedImages.removeAll(finalImages);

    final SortedSet<String> removedVideos = new TreeSet<>(setupInfo.getVideos());
    removedVideos.removeAll(finalVideos);

    for (final var videoContent : removedVideos) {
      final String fileName = videoContent.substring(videoContent.lastIndexOf('/') + 1);
      this.setupStorageService.deleteContent(user.getId(), setupId, fileName, VIDEO_PATH);
    }

    for (final var imageContent : removedImages) {
      final String fileName = imageContent.substring(imageContent.lastIndexOf('/') + 1);
      this.setupStorageService.deleteContent(user.getId(), setupId, fileName, IMAGE_PATH);
    }

    return this.setupService.updateSetup(setupId, user, setupForm, finalImages, finalVideos);
  }

  public @NotNull SetupInfo createSetupByUserProfile(
      final @NotNull String userId,
      final @NotNull ProfileSetupForm setupForm,
      final @NotNull List<MultipartFile> images) {
    final var user = this.findUserById(userId);
    final var setupId = UlidCreator.getUlid().toString();
    setupForm.setSetupId(setupId);

    log.warn(
        "User with id {} and username: {} has updated profile setup info",
        userId,
        user.getUsername());

    final var newImageUrls =
        new TreeSet<>(
            this.setupStorageService.writeContents(user.getId(), setupId, images, IMAGE_PATH));

    final var finalImages =
        Stream.concat(setupForm.getExistingImages().stream(), newImageUrls.stream())
            .collect(Collectors.toCollection(TreeSet::new));

    return this.setupService.createSetup(user, setupForm, finalImages);
  }

  public void like(final @NotNull String setupId, final @NotNull String userId) {
    final var user = this.findUserById(userId);
    final var setup = this.setupService.like(setupId, user);

    if (setup.getUser().getId().equals(user.getId())) {
      return;
    }

    final var form =
        NotificationForm.builder()
            .to(setup.getUser().getId())
            .title("New Like")
            .type(NotificationType.LIKE)
            .description("%s liked your setup".formatted(user.getUsername()))
            .action(String.format("/%s/%s", "setups", setup.getId()))
            .build();

    this.notificationService.createNotification(form, user.getId());
  }

  public void unlike(final @NotNull String setupId, final @NotNull String userId) {
    final var user = this.findUserById(userId);

    this.setupService.unlike(setupId, user);
  }

  public CommentInfo addComment(
      final @NotNull String setupId,
      final @NotNull String userId,
      final @NotNull CommentForm commentForm) {
    final var user = this.findUserById(userId);
    final var commentInfo = this.setupService.addComment(setupId, user, commentForm);
    final var setupOwner = this.setupService.findSetupOwnerBySetupId(setupId);

    if (setupOwner.getId().equals(user.getId())) {
      return commentInfo;
    }

    final var form =
        NotificationForm.builder()
            .to(setupOwner.getId())
            .title("New Comment")
            .description(commentInfo.getAuthor().getUsername() + " commented on your setup")
            .type(NotificationType.COMMENT)
            .action(String.format("/%s/%s", "setups", setupId))
            .build();

    this.notificationService.createNotification(form, user.getId());

    return commentInfo;
  }

  public void deleteComment(
      final @NotNull String setupId,
      final @NotNull String commentId,
      final @NotNull String userId) {
    final var user = this.findUserById(userId);

    this.setupService.deleteComment(setupId, commentId, user);
  }

  public CommentInfo editComment(
      final @NotNull String setupId,
      final @NotNull String userId,
      final @NotNull String commentId,
      final @NotNull CommentForm commentForm) {
    final var user = this.findUserById(userId);
    return this.setupService.editComment(setupId, user, commentId, commentForm);
  }

  private @NotNull User findUserById(final @NotNull String userId) {
    return this.userRepository
        .findUserById(userId)
        .orElseThrow(() -> new IllegalArgumentException("userNotFound"));
  }

  public @NotNull List<SetupInfo> exploreSetups(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    final var user = this.findUserById(userId);
    return this.setupService.exploreSetups(user, pageable);
  }

  public @NotNull List<SetupInfo> feedSetups(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    final var user = this.findUserById(userId);
    return this.setupService.feedSetups(user, pageable);
  }

  public @NotNull List<CommentInfo> getComments(
      final @NotNull String userId,
      final @NotNull String setupId,
      final @NotNull Pageable pageable) {
    final var user = this.findUserById(userId);
    return this.setupService.getComments(setupId, user, pageable);
  }

  public @NotNull List<SetupInfo> getSetupFavoriteSetups(
      final @NotNull String userId, final @NotNull Pageable pageable) {
    return this.setupService.getSetupFavoriteSetups(this.findUserById(userId), pageable);
  }

  public void likeComment(
      final @NotNull String setupId,
      final @NotNull String commentId,
      final @NotNull String userId) {
    final var user = this.findUserById(userId);
    final var comment = this.setupService.likeComment(setupId, user, commentId);

    if (comment.getUser().getId().equals(user.getId())) {
      return;
    }

    final var form =
        NotificationForm.builder()
            .to(comment.getUser().getId())
            .title("New Comment Like")
            .type(NotificationType.LIKE)
            .description(
                "%s liked your mentioned. Comment is: %s"
                    .formatted(user.getUsername(), comment.getContent()))
            .action(String.format("/%s/%s", "setups", setupId))
            .build();

    this.notificationService.createNotification(form, user.getId());
  }

  public void unlikeComment(final @NotNull String commentId, final @NotNull String userId) {
    final var user = this.findUserById(userId);

    this.setupService.unlikeComment(commentId, user);
  }
}

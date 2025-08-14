package com.setupshowroom.shared.contentmoderation;

import com.setupshowroom.shared.contentmoderation.dto.InappropriateReason;
import com.setupshowroom.shared.contentmoderation.dto.Medical;
import com.setupshowroom.shared.contentmoderation.dto.Nudity;
import com.setupshowroom.shared.contentmoderation.dto.RecreationalDrug;
import com.setupshowroom.shared.contentmoderation.dto.SelfHarm;
import com.setupshowroom.shared.contentmoderation.dto.SightEngineVideoResponse;
import com.setupshowroom.shared.contentmoderation.dto.SightengineImageResponse;
import com.setupshowroom.shared.exceptionhandler.exception.ModerationFailedException;
import com.setupshowroom.shared.exceptionhandler.exception.UnPermittedContentException;
import com.setupshowroom.user.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationService {
  private static final String STATUS_SC = "success";
  private static final double DEFAULT_THRESHOLD = 0.5;
  private static final double EROTICA_THRESHOLD = 0.6;
  private static final double MILDLY_SUGGESTIVE_THRESHOLD = 0.7;

  private final @NotNull ContentModerationConfigProps contentModerationConfigProps;
  private final @NotNull ContentModerationRemoteService contentModerationRemoteService;

  public void checkContentValidity(
      final @NotNull User user,
      final @Nullable List<MultipartFile> images,
      final @Nullable List<MultipartFile> videos) {
    try {
      if (!this.contentModerationConfigProps.isActive()) {
        return;
      }

      if (images != null && !images.isEmpty()) {
        this.checkImageContent(images);
      }

      if (videos != null && !videos.isEmpty()) {
        this.checkVideoContent(videos);
      }
    } catch (final ModerationFailedException e) {
      log.warn(
          "Dangerous content detected: {} : {}\nMessage: {}\nTimestamp: {}",
          user.getUsername(),
          user.getEmail(),
          e.getMessage(),
          Instant.now());

      throw new UnPermittedContentException(e.getMessage());
    }
  }

  private void checkImageContent(final @NotNull List<MultipartFile> images) {
    for (final var image : images) {
      final var response =
          this.contentModerationRemoteService.analyzeImage(
              image,
              this.contentModerationConfigProps.getModels(),
              this.contentModerationConfigProps.getApiUser(),
              this.contentModerationConfigProps.getApiSecret());

      this.checkResponse(response, image);
    }
  }

  private void checkVideoContent(final @NotNull List<MultipartFile> videos) {
    for (final var video : videos) {
      final var response =
          this.contentModerationRemoteService.analyzeVideo(
              video,
              this.contentModerationConfigProps.getModels(),
              this.contentModerationConfigProps.getApiUser(),
              this.contentModerationConfigProps.getApiSecret());

      this.checkResponse(response, video);
    }
  }

  private void checkResponse(
      final @NotNull SightEngineVideoResponse response, final @NotNull MultipartFile content) {
    if (!response.status.equals(STATUS_SC)) {
      return;
    }

    final var frames = response.data.frames;
    if (frames == null || frames.isEmpty()) {
      return;
    }

    for (final var frame : frames) {
      final var appropriate =
          this.isInappropriate(frame.nudity, frame.medical, frame.recreationalDrug, frame.selfHarm);

      if (appropriate.isPresent()) {
        throw new ModerationFailedException(content.getOriginalFilename(), appropriate.get());
      }
    }
  }

  private void checkResponse(
      final @NotNull SightengineImageResponse response, final @NotNull MultipartFile content) {
    if (!response.status.equals(STATUS_SC)) {
      return;
    }

    final var approvedContent =
        this.isInappropriate(
            response.nudity, response.medical, response.recreationalDrug, response.selfHarm);

    if (approvedContent.isPresent()) {
      throw new ModerationFailedException(content.getOriginalFilename(), approvedContent.get());
    }
  }

  private @NotNull Optional<String> isInappropriate(
      final @NotNull Nudity nudity,
      final @NotNull Medical medical,
      final @NotNull RecreationalDrug recreationalDrug,
      final @NotNull SelfHarm selfHarm) {
    if (this.isNudityViolation(nudity)) {
      return Optional.of(InappropriateReason.NUDITY.getReason());
    }

    if (medical.prob > DEFAULT_THRESHOLD) {
      return Optional.of(InappropriateReason.MEDICAL.getReason());
    }

    if (recreationalDrug.prob > DEFAULT_THRESHOLD) {
      return Optional.of(InappropriateReason.DRUGS.getReason());
    }

    if (selfHarm.prob > DEFAULT_THRESHOLD) {
      return Optional.of(InappropriateReason.SELF_HARM.getReason());
    }

    return Optional.empty();
  }

  private boolean isNudityViolation(final @NotNull Nudity nudity) {
    return nudity.sexual_activity > DEFAULT_THRESHOLD
        || nudity.sexual_display > DEFAULT_THRESHOLD
        || nudity.very_suggestive > DEFAULT_THRESHOLD
        || nudity.mildly_suggestive > MILDLY_SUGGESTIVE_THRESHOLD
        || nudity.erotica > EROTICA_THRESHOLD;
  }
}

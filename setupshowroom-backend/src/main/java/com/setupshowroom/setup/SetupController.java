package com.setupshowroom.setup;

import com.setupshowroom.comment.dto.CommentForm;
import com.setupshowroom.comment.dto.CommentInfo;
import com.setupshowroom.setup.dto.ProfileSetupForm;
import com.setupshowroom.setup.dto.SetupForm;
import com.setupshowroom.setup.dto.SetupInfo;
import com.setupshowroom.setup.dto.SetupUpdateForm;
import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.shared.security.BearerTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/setup")
@RestController
@RequiredArgsConstructor
public class SetupController {
  private final @NotNull SetupFacade setupFacade;
  private final @NotNull BearerTokenService bearerTokenService;

  @PostMapping
  public ResponseEntity<Response<SetupInfo>> create(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @RequestPart("setup") @Valid final @NotNull SetupForm setupForm,
      @RequestPart(value = "images", required = false) final List<MultipartFile> images,
      @RequestPart(value = "videos", required = false) final List<MultipartFile> videos) {
    final String userId = this.getUserId(authHeader);
    final var userInfo = this.setupFacade.createSetup(userId, setupForm, images, videos);

    final var response = Response.success("created", userInfo, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{userId}/setup/{setupId}")
  public ResponseEntity<Response<SetupInfo>> update(
      @PathVariable final @NotNull String setupId,
      @PathVariable final @NotNull String userId,
      @RequestPart("setup") @Valid final @NotNull SetupUpdateForm setupForm,
      @RequestPart(value = "images", required = false) final List<MultipartFile> images,
      @RequestPart(value = "videos", required = false) final List<MultipartFile> videos) {
    setupForm.setExistingImages(
        setupForm.getExistingImages() == null ? List.of() : setupForm.getExistingImages());

    setupForm.setExistingVideos(
        setupForm.getExistingVideos() == null ? List.of() : setupForm.getExistingVideos());

    final var userInfo = this.setupFacade.updateSetup(setupId, userId, setupForm, images, videos);

    final var response = Response.success("updated", userInfo, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/setup-by-profile")
  public ResponseEntity<Response<SetupInfo>> upsertSetupByProfile(
      @PathVariable final @NotNull String userId,
      @RequestPart("setup") final @NotNull ProfileSetupForm setupForm,
      @RequestPart(value = "images", required = false) final List<MultipartFile> images) {
    setupForm.setExistingImages(
        setupForm.getExistingImages() == null ? List.of() : setupForm.getExistingImages());
    final var setupInfo = this.setupFacade.createSetupByUserProfile(userId, setupForm, images);
    final var response = Response.success("created", setupInfo, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{setupId}")
  public ResponseEntity<Response<?>> delete(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId) {
    this.setupFacade.deleteSetupById(setupId, this.getUserId(authHeader));
    final var response = Response.success("deleted", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/setups")
  public ResponseEntity<Response<List<SetupInfo>>> getSetupsByUserId(
      @PathVariable final @NotNull String userId,
      @PageableDefault(sort = "embeddedTimestamps.updatedAt", direction = Sort.Direction.DESC)
          final @NotNull Pageable pageable) {
    final List<SetupInfo> setups = this.setupFacade.findAllSetupsByUser(userId, pageable);
    final Response<List<SetupInfo>> response =
        Response.success("retrieved", setups, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/setups/{setupId}")
  public ResponseEntity<Response<SetupInfo>> getSetup(
      @PathVariable final @NotNull String userId, @PathVariable final @NotNull String setupId) {
    final var setupInfo = this.setupFacade.findSetupById(userId, setupId);
    final var response = Response.success("retrieved", setupInfo, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{setupId}/comments")
  public ResponseEntity<Response<List<CommentInfo>>> getComments(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId,
      @PageableDefault final @NotNull Pageable pageable) {
    final var userId = this.getUserId(authHeader);
    final List<CommentInfo> comments = this.setupFacade.getComments(userId, setupId, pageable);

    final Response<List<CommentInfo>> response =
        Response.success("retrieved", comments, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{setupId}/comment")
  public ResponseEntity<Response<CommentInfo>> comment(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @RequestBody @Valid final @NotNull CommentForm commentForm,
      @PathVariable final @NotNull String setupId) {
    final String userId = this.getUserId(authHeader);

    final var commentInfo = this.setupFacade.addComment(setupId, userId, commentForm);

    final var response = Response.success("commented", commentInfo, HttpStatus.OK.value());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PutMapping("/{setupId}/comment/{commentId}")
  public ResponseEntity<Response<CommentInfo>> editComment(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @RequestBody @Valid final @NotNull CommentForm commentForm,
      @PathVariable final @NotNull String setupId,
      @PathVariable final @NotNull String commentId) {
    final String userId = this.getUserId(authHeader);
    final var info = this.setupFacade.editComment(setupId, userId, commentId, commentForm);
    return ResponseEntity.ok(Response.success("edited", info, HttpStatus.OK.value()));
  }

  @DeleteMapping("/{setupId}/comment/{commentId}")
  public ResponseEntity<Response<?>> deleteComment(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId,
      @PathVariable final @NotNull String commentId) {
    final String userId = this.getUserId(authHeader);
    this.setupFacade.deleteComment(setupId, commentId, userId);

    final var response = Response.success("deleted", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{setupId}/like")
  public ResponseEntity<Response<?>> like(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId) {
    final String userId = this.getUserId(authHeader);
    this.setupFacade.like(setupId, userId);
    final var response = Response.success("liked", null, HttpStatus.OK.value());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/{setupId}/unlike")
  public ResponseEntity<Response<?>> unlike(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId) {
    final String userId = this.getUserId(authHeader);
    this.setupFacade.unlike(setupId, userId);

    final var response = Response.success("unliked", null, HttpStatus.OK.value());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/{setupId}/favorite")
  public ResponseEntity<Response<?>> favorite(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId) {
    final String userId = this.getUserId(authHeader);
    this.setupFacade.addToFavorite(setupId, userId);
    final var response = Response.success("added", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{setupId}/unfavorite")
  public ResponseEntity<Response<?>> deleteFavorite(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PathVariable final @NotNull String setupId) {
    final String userId = this.getUserId(authHeader);
    this.setupFacade.removeFromFavorite(setupId, userId);
    final var response = Response.success("removed", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/setups/{setupId}/comments/{commentId}/like")
  public ResponseEntity<Response<?>> likeComment(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String setupId,
      @PathVariable final @NotNull String commentId) {
    this.setupFacade.likeComment(setupId, commentId, userId);
    final var response = Response.success("liked", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{userId}/setups/{setupId}/comments/{commentId}/unlike")
  @SuppressWarnings("unused")
  public ResponseEntity<Response<?>> unlikeComment(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String setupId,
      @PathVariable final @NotNull String commentId) {
    this.setupFacade.unlikeComment(commentId, userId);
    final var response = Response.success("unliked", null, HttpStatus.OK.value());
    return ResponseEntity.ok(response);
  }

  private @NotNull String getUserId(final @NotNull String authHeader) {
    return this.bearerTokenService.extractUserId(authHeader.substring("Bearer ".length()));
  }
}

package com.setupshowroom.setup;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.comment.Comment;
import com.setupshowroom.comment.CommentLike;
import com.setupshowroom.comment.CommentLikeRepository;
import com.setupshowroom.comment.CommentRepository;
import com.setupshowroom.comment.dto.CommentForm;
import com.setupshowroom.comment.dto.CommentInfo;
import com.setupshowroom.setup.converter.SetupConverter;
import com.setupshowroom.setup.dto.ProfileSetupForm;
import com.setupshowroom.setup.dto.SetupForm;
import com.setupshowroom.setup.dto.SetupInfo;
import com.setupshowroom.setup.dto.SetupUpdateForm;
import com.setupshowroom.shared.exceptionhandler.exception.AccessNotAllowedException;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.tag.Tag;
import com.setupshowroom.tag.TagRepository;
import com.setupshowroom.user.FavoriteRepository;
import com.setupshowroom.user.User;
import com.setupshowroom.user.converter.UserConverter;
import com.setupshowroom.user.dto.UserInfo;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class SetupService {
  private final @NotNull SetupRepository setupRepository;
  private final @NotNull LikeRepository likeRepository;
  private final @NotNull CommentLikeRepository commentLikeRepository;
  private final @NotNull CommentRepository commentRepository;
  private final @NotNull FavoriteRepository favoriteRepository;
  private final @NotNull TagRepository tagRepository;
  private final @NotNull SetupConverter setupConverter;
  private final @NotNull UserConverter userConverter;

  public @NotNull SetupInfo createSetup(
      final @NotNull User user,
      final @NotNull SetupForm setupForm,
      final @NotNull List<String> images,
      final @NotNull List<String> videos) {
    final var tags = setupForm.getTags().stream().map(this.setupConverter::toTag).toList();
    final List<Tag> savedTags = this.tagRepository.saveAll(tags);

    final Setup setup = this.setupConverter.toSetup(setupForm);
    setup.setId(setupForm.getSetupId());
    setup.setVideos(new TreeSet<>(videos));
    setup.setImages(new TreeSet<>(images));
    setup.setTags(new TreeSet<>(savedTags));
    setup.setUser(user);

    final var savedUser = this.setupRepository.save(setup);

    return this.toSetupInfo(user, savedUser, user);
  }

  public @NotNull SetupInfo createSetup(
      final @NotNull User user,
      final @NotNull ProfileSetupForm setupForm,
      final @NotNull SortedSet<String> images) {
    final var tags = setupForm.getTags().stream().map(this.setupConverter::toTag).toList();
    final List<Tag> savedTags = this.tagRepository.saveAll(tags);

    final Setup setup = new Setup();
    setup.setTitle(setupForm.getTitle());
    setup.setId(setupForm.getSetupId());
    setup.setDescription(setupForm.getDescription());
    setup.setImages(new TreeSet<>(images));
    setup.setTags(new TreeSet<>(savedTags));
    setup.setUser(user);
    setup.setCategories(
        setupForm.getCategories().stream()
            .map(SetupCategory::valueOf)
            .collect(Collectors.toCollection(TreeSet::new)));

    final var savedUser = this.setupRepository.save(setup);

    return this.toSetupInfo(user, savedUser, user);
  }

  public void deleteSetupById(final @NotNull User user, final @NotNull String setupId) {
    final Setup setup =
        this.setupRepository
            .findSetupById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    if (!setup.getUser().equals(user)) {
      throw new AccessNotAllowedException("accessDenied");
    }

    setup.setDeleted(true);
    setup.getEmbeddedTimestamps().setDeletedAt(Instant.now());

    this.setupRepository.save(setup);
  }

  public @NotNull List<SetupInfo> findAllSetupsByUser(
      final @NotNull User user, final @NotNull Pageable pageable) {
    return this.setupRepository.findAllByUserIdAndDeletedFalse(user.getId(), pageable).stream()
        .map(s -> this.toSetupInfo(s.getUser(), s, user))
        .toList();
  }

  public @NotNull SetupInfo findSetupById(final @NotNull User user, final @NotNull String setupId) {
    final Setup setup =
        this.setupRepository
            .findSetupById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    return this.toSetupInfo(setup.getUser(), setup, user);
  }

  public void addToFavorite(final @NotNull String setupId, final @NotNull User user) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final boolean isExist =
        this.favoriteRepository.existsByUserIdAndSetupIdAndDeletedFalse(
            user.getId(), setup.getId());

    if (!isExist) {
      final Favorite favorite =
          Favorite.builder().id(UlidCreator.getUlid().toString()).user(user).setup(setup).build();

      this.favoriteRepository.save(favorite);
    }
  }

  public void removeFromFavorite(final @NotNull String setupId, final @NotNull User user) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Favorite favorite =
        this.favoriteRepository
            .findByUserIdAndSetupIdAndDeletedFalse(user.getId(), setup.getId())
            .orElseThrow(() -> new ItemNotFoundException("favoriteNotFound"));

    this.favoriteRepository.delete(favorite);
  }

  public @NotNull SetupInfo updateSetup(
      final @NotNull String setupId,
      final @NotNull User user,
      final @NotNull SetupUpdateForm setupForm,
      final @NotNull SortedSet<String> imagesSet,
      final @NotNull SortedSet<String> videosSet) {
    final Setup setup =
        this.setupRepository
            .findSetupById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    if (!setup.getUser().equals(user)) {
      throw new AccessNotAllowedException("accessDenied");
    }

    setup.setTitle(setupForm.getTitle());
    setup.setDescription(setupForm.getDescription());
    setup.setVideos(videosSet);
    setup.setImages(imagesSet);
    setup.setCategories(
        setupForm.getCategories().stream()
            .map(SetupCategory::valueOf)
            .collect(Collectors.toCollection(TreeSet::new)));

    final var tags = setupForm.getTags().stream().map(this.setupConverter::toTag).toList();

    final List<Tag> savedTags = this.tagRepository.saveAll(tags);
    setup.setTags(new TreeSet<>(savedTags));

    final Setup savedSetup = this.setupRepository.save(setup);

    return this.toSetupInfo(savedSetup.getUser(), savedSetup, user);
  }

  public @NotNull Setup like(final @NotNull String setupId, final @NotNull User user) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Optional<Like> existLike =
        this.likeRepository.findByUserIdAndSetupIdAndDeletedFalse(user.getId(), setup.getId());

    if (existLike.isEmpty()) {
      final Like like =
          Like.builder().id(UlidCreator.getUlid().toString()).user(user).setup(setup).build();

      this.likeRepository.save(like);
    }

    return setup;
  }

  public void unlike(final @NotNull String setupId, final @NotNull User user) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Like like =
        this.likeRepository
            .findByUserIdAndSetupIdAndDeletedFalse(user.getId(), setup.getId())
            .orElseThrow(() -> new ItemNotFoundException("likeNotFound"));

    this.likeRepository.delete(like);
  }

  public CommentInfo addComment(
      final @NotNull String setupId,
      final @NotNull User user,
      final @NotNull CommentForm commentForm) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Comment comment =
        Comment.builder()
            .id(UlidCreator.getUlid().toString())
            .user(user)
            .setup(setup)
            .content(commentForm.getContent())
            .build();

    comment.setEmbeddedTimestamps(new EmbeddedTimestamps());
    comment.getEmbeddedTimestamps().setCreatedAt(Instant.now());

    setup.addComment(comment);

    this.setupRepository.save(setup);

    return this.setupConverter.toCommentInfo(comment, this.userConverter.toUserInfo(user));
  }

  public void deleteComment(
      final @NotNull String setupId, final @NotNull String commentId, final @NotNull User user) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Comment comment =
        this.commentRepository
            .findByIdAndUserIdAndSetupIdAndDeletedFalse(commentId, user.getId(), setup.getId())
            .orElseThrow(() -> new ItemNotFoundException("commentNotFound"));

    comment.setDeleted(true);
    comment.getEmbeddedTimestamps().setDeletedAt(Instant.now());

    this.commentRepository.save(comment);
  }

  public CommentInfo editComment(
      final @NotNull String setupId,
      final @NotNull User user,
      final @NotNull String commentId,
      final @NotNull CommentForm commentForm) {
    final Setup setup =
        this.setupRepository
            .findById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    final Comment comment =
        this.commentRepository
            .findByIdAndUserIdAndSetupIdAndDeletedFalse(commentId, user.getId(), setup.getId())
            .orElseThrow(() -> new ItemNotFoundException("commentNotFound"));

    comment.setContent(commentForm.getContent());

    final var editedComment = this.commentRepository.save(comment);

    return this.setupConverter.toCommentInfo(editedComment, this.userConverter.toUserInfo(user));
  }

  public @NotNull List<SetupInfo> exploreSetups(
      final @NotNull User authorizedUser, final @NotNull Pageable pageable) {
    final var setups = this.setupRepository.exploreSetups(pageable);

    return setups.stream().map(s -> this.toSetupInfo(s.getUser(), s, authorizedUser)).toList();
  }

  public @NotNull List<SetupInfo> feedSetups(
      final @NotNull User authorizedUser, final @NotNull Pageable pageable) {
    final var setups = this.setupRepository.feedSetups(pageable);

    return setups.stream().map(s -> this.toSetupInfo(s.getUser(), s, authorizedUser)).toList();
  }

  public @NotNull List<CommentInfo> getComments(
      final @NotNull String setupId, final @NotNull User user, final @NotNull Pageable pageable) {
    return this.commentRepository.findAllCommentInfoBySetupIdAndDeletedFalse(
        setupId, user.getId(), pageable);
  }

  public @NotNull List<SetupInfo> getSetupFavoriteSetups(
      final @NotNull User user, final @NotNull Pageable pageable) {
    final List<Setup> setups =
        this.setupRepository.findAllFavoritesByUserId(user.getId(), pageable);

    return setups.stream().map(s -> this.toSetupInfo(s.getUser(), s, user)).toList();
  }

  private @NotNull SetupInfo toSetupInfo(
      final @NotNull User setupOwner,
      final @NotNull Setup setup,
      final @NotNull User authorizedUser) {
    final UserInfo setupOwnerInfo = this.userConverter.toUserInfo(setupOwner);

    final var commentSize = this.commentRepository.countBySetupIdAndDeletedFalse(setup.getId());

    final var likeCount = this.likeRepository.countBySetupIdAndDeletedFalse(setup.getId());

    final var isUserLiked =
        this.likeRepository.existsByUserIdAndSetupIdAndDeletedFalse(
            authorizedUser.getId(), setup.getId());

    final var isFav =
        this.favoriteRepository.existsByUserIdAndSetupIdAndDeletedFalse(
            authorizedUser.getId(), setup.getId());

    return SetupInfo.builder()
        .id(setup.getId())
        .description(setup.getDescription())
        .userInfo(setupOwnerInfo)
        .createdAt(setup.getEmbeddedTimestamps().getCreatedAt())
        .title(setup.getTitle())
        .categories(setup.getCategories().stream().map(SetupCategory::name).toList())
        .images(List.copyOf(setup.getImages()))
        .videos(List.copyOf(setup.getVideos()))
        .tags(setup.getTags().stream().map(Tag::getName).toList())
        .likes(likeCount)
        .isLiked(isUserLiked)
        .commentSize(commentSize)
        .isFavorite(isFav)
        .build();
  }

  @SuppressWarnings("unused")
  private void hardDeleteSetup(final @NotNull User user, final @NotNull String setupId) {
    final Setup setup =
        this.setupRepository
            .findSetupById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    if (!setup.getUser().equals(user)) {
      throw new AccessNotAllowedException("accessDenied");
    }

    final Instant deletedAt = Instant.now();

    setup
        .getComments()
        .forEach(
            c -> {
              c.getEmbeddedTimestamps().setDeletedAt(deletedAt);
              c.setDeleted(true);
            });
    setup
        .getFavorites()
        .forEach(
            f -> {
              f.getTimestamps().setDeletedAt(deletedAt);
              f.setDeleted(true);
            });
    setup
        .getLikes()
        .forEach(
            l -> {
              l.setDeleted(true);
              l.getEmbeddedTimestamps().setDeletedAt(deletedAt);
            });

    setup.setDeleted(true);
    setup.getEmbeddedTimestamps().setDeletedAt(Instant.now());
    setup.getCategories().clear();
    setup.getTags().clear();
    setup.getImages().clear();
    setup.getVideos().clear();

    this.setupRepository.delete(setup);
  }

  public @NotNull UserInfo findSetupOwnerBySetupId(final @NotNull String setupId) {
    final var owner =
        this.setupRepository
            .findSetupById(setupId)
            .orElseThrow(() -> new ItemNotFoundException("setupNotFound"));

    return this.userConverter.toUserInfo(owner.getUser());
  }

  public @NotNull Comment likeComment(
      final @NotNull String setupId, final @NotNull User user, final @NotNull String commentId) {
    final Comment comment =
        this.commentRepository
            .findByIdAndSetupIdAndDeletedFalse(commentId, setupId)
            .orElseThrow(() -> new ItemNotFoundException("commentNotFound"));

    final Optional<CommentLike> existLike =
        this.commentLikeRepository.findByUserIdAndCommentIdAndDeletedFalse(user.getId(), commentId);

    if (existLike.isEmpty()) {
      final CommentLike like =
          CommentLike.builder()
              .id(UlidCreator.getUlid().toString())
              .user(user)
              .comment(comment)
              .build();

      this.commentLikeRepository.save(like);
    }

    return comment;
  }

  public void unlikeComment(final @NotNull String commentId, final @NotNull User user) {
    final CommentLike like =
        this.commentLikeRepository
            .findByUserIdAndCommentIdAndDeletedFalse(user.getId(), commentId)
            .orElseThrow(() -> new ItemNotFoundException("likeNotFound"));

    this.commentLikeRepository.delete(like);
  }
}

package com.setupshowroom.comment;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, String> {
  @NotNull
  Optional<CommentLike> findByUserIdAndCommentIdAndDeletedFalse(
      @NotNull String userId, @NotNull String commentId);
}

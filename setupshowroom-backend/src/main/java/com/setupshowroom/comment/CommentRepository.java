package com.setupshowroom.comment;

import com.setupshowroom.comment.dto.CommentInfo;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
  @NotNull
  Optional<Comment> findByIdAndUserIdAndSetupIdAndDeletedFalse(
      @NotNull String id, @NotNull String userId, @NotNull String setupId);

  @NotNull
  Optional<Comment> findByIdAndSetupIdAndDeletedFalse(@NotNull String id, @NotNull String setupId);

  long countBySetupIdAndDeletedFalse(String id);

  @Query(
      value =
          """
            select new com.setupshowroom.comment.dto.CommentInfo(
                c.id,
                new com.setupshowroom.user.dto.UserInfo(
                    c.user.id,
                    c.user.fullName,
                    c.user.email,
                    c.user.username,
                    c.user.profession,
                    c.user.enabled
                ),
                c.content,
                c.embeddedTimestamps.createdAt,
                c.embeddedTimestamps.updatedAt,
                case when exists (select 1 from CommentLike cl
                            where cl.comment.id = c.id and cl.user.id = :userId)
                                        then true else false end,
                (select count (cl) from CommentLike cl where cl.comment.id = c.id and cl.deleted = false)
            )
            from Comment c
            where c.setup.id = :setupId and c.deleted = false
            order by c.embeddedTimestamps.createdAt desc, c.id desc
            """)
  @NotNull
  List<CommentInfo> findAllCommentInfoBySetupIdAndDeletedFalse(
      @NotNull String setupId, @NotNull String userId, @NotNull Pageable pageable);
}

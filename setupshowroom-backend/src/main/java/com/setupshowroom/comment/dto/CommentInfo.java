package com.setupshowroom.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.setupshowroom.user.dto.UserInfo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class CommentInfo {
  private String id;
  private UserInfo author;
  private String content;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;

  @JsonProperty("is_liked")
  private boolean isLiked;

  @JsonProperty("like_count")
  private long likeCount;
}

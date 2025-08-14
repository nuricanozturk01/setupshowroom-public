package com.setupshowroom.setup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.setupshowroom.user.dto.UserInfo;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SetupInfo {
  private String title;
  private List<String> categories;
  private List<String> images;
  private List<String> videos;
  private List<String> tags;
  private long likes;
  private String id;
  private String description;

  @JsonProperty("user_info")
  private UserInfo userInfo;

  @JsonProperty("comment_size")
  private long commentSize;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("is_liked")
  private boolean isLiked;

  @JsonProperty("is_favorite")
  private boolean isFavorite;

  @JsonProperty("owner_setup")
  private boolean ownerSetup;
}

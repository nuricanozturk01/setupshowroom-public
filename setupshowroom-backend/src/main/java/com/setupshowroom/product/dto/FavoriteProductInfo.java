package com.setupshowroom.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
public class FavoriteProductInfo {
  @JsonProperty("group_id")
  private String groupId;

  private String id;

  private String name;

  private String url;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;
}

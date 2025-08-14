package com.setupshowroom.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FavoriteProductGroupInfo {
  private String id;

  private String name;

  private List<FavoriteProductInfo> products;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;
}

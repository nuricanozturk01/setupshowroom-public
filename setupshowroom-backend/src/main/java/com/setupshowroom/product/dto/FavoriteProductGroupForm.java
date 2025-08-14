package com.setupshowroom.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FavoriteProductGroupForm {
  @NotEmpty
  @Size(min = 5, max = 150)
  private String name;

  private List<FavoriteProductForm> products;
}

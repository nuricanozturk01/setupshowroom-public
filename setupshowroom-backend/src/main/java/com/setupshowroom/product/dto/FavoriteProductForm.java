package com.setupshowroom.product.dto;

import com.setupshowroom.product.validator.Url;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FavoriteProductForm {
  @NotEmpty
  @Size(min = 5, max = 150)
  private String name;

  @NotEmpty
  @Size(min = 3, max = 255)
  @Url
  private String url;
}

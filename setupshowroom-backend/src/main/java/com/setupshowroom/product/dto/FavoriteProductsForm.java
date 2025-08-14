package com.setupshowroom.product.dto;

import java.util.List;
import lombok.Data;

@Data
public class FavoriteProductsForm {
  private List<FavoriteProductForm> products;
}

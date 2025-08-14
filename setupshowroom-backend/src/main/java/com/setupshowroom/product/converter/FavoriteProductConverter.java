package com.setupshowroom.product.converter;

import com.setupshowroom.product.FavoriteProduct;
import com.setupshowroom.product.FavoriteProductGroup;
import com.setupshowroom.product.dto.FavoriteProductForm;
import com.setupshowroom.product.dto.FavoriteProductGroupInfo;
import com.setupshowroom.product.dto.FavoriteProductInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FavoriteProductConverter {

  @Mappings({
    @Mapping(source = "timestamps.createdAt", target = "createdAt"),
    @Mapping(source = "timestamps.updatedAt", target = "updatedAt"),
  })
  @NotNull
  FavoriteProductInfo toFavoriteProductInfo(@NotNull FavoriteProduct favoriteProduct);

  @Mapping(
      target = "id",
      expression = "java(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString())")
  FavoriteProduct toFavoriteProduct(@NotNull FavoriteProductForm form);

  default @NotNull FavoriteProductGroupInfo toFavoriteProductGroupInfo(
      @NotNull FavoriteProductGroup group, @NotNull List<FavoriteProductInfo> products) {
    return FavoriteProductGroupInfo.builder()
        .id(group.getId())
        .name(group.getName())
        .products(products)
        .build();
  }
}

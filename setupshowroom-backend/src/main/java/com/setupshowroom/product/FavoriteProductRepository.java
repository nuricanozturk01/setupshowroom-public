package com.setupshowroom.product;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.SortedSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, String> {
  @NotNull
  SortedSet<FavoriteProduct>
      findAllByFavoriteProductGroupIdAndFavoriteProductGroupUserIdAndDeletedFalse(
          String groupId, String userId);

  @NotNull
  Optional<FavoriteProduct> findByIdAndFavoriteProductGroupIdAndFavoriteProductGroupUserId(
      @NotNull String groupId, @NotNull String productId, @NotNull String userId);
}

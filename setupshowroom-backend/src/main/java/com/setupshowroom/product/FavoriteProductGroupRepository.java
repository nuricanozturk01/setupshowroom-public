package com.setupshowroom.product;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.SortedSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteProductGroupRepository
    extends JpaRepository<FavoriteProductGroup, String> {
  @NotNull
  SortedSet<FavoriteProductGroup> findAllByUserIdAndDeletedFalse(@NotNull String userId);

  @NotNull
  Optional<FavoriteProductGroup> findByUserIdAndNameAndDeletedFalse(
      @NotNull String userId, @NotNull String name);

  @NotNull
  Optional<FavoriteProductGroup> findByUserIdAndIdAndDeletedFalse(
      @NotNull String userId, @NotNull String id);
}

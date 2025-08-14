package com.setupshowroom.user;

import com.setupshowroom.setup.Favorite;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
  @NotNull
  Optional<Favorite> findByUserIdAndSetupIdAndDeletedFalse(
      @NotNull String userId, @NotNull String setupId);

  boolean existsByUserIdAndSetupIdAndDeletedFalse(String userId, String setupId);
}

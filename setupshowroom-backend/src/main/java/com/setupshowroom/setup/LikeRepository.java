package com.setupshowroom.setup;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
  Optional<Like> findByUserIdAndSetupIdAndDeletedFalse(String userId, String setupId);

  long countBySetupIdAndDeletedFalse(String id);

  boolean existsByUserIdAndSetupIdAndDeletedFalse(String userId, String setupId);
}

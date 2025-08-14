package com.setupshowroom.setup;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SetupRepository extends JpaRepository<Setup, String> {
  @Query(
      """
    select s from Setup s
    left join fetch s.user
    where s.deleted = false
    and s.user.deleted = false
    and s.user.id = :userId
    """)
  @NotNull
  Page<Setup> findAllByUserIdAndDeletedFalse(String userId, Pageable pageable);

  @Query(
      """
      SELECT s
      FROM Setup s
      left join fetch s.user
      WHERE s.deleted = false
      ORDER BY (SIZE(s.likes) + SIZE(s.comments) + SIZE(s.favorites)) DESC,
               s.embeddedTimestamps.updatedAt DESC,
               s.embeddedTimestamps.createdAt DESC
      """)
  @NotNull
  Page<Setup> exploreSetups(Pageable pageable);

  @Query(
      """
      SELECT s
      FROM Setup s
      join fetch s.user
      WHERE s.deleted = false
      ORDER BY (SIZE(s.likes) + SIZE(s.comments) + SIZE(s.favorites)) DESC,
               s.embeddedTimestamps.updatedAt DESC,
               s.embeddedTimestamps.createdAt DESC
      """)
  @NotNull
  Page<Setup> feedSetups(Pageable pageable);

  @Query(
      """
      SELECT s
      FROM Setup s
      JOIN s.favorites f
      left join fetch s.user
      WHERE s.deleted = false
      AND f.user.id = :userId
      AND f.deleted = false
      ORDER BY s.embeddedTimestamps.createdAt DESC
      """)
  @NotNull
  List<Setup> findAllFavoritesByUserId(String userId, @NotNull Pageable pageable);

  @Query(
      """
    select s from Setup s
    left join fetch s.user
    where s.deleted = false
    and s.user.deleted = false
    and s.id = :setupId
    """)
  Optional<Setup> findSetupById(@NotNull String setupId);
}

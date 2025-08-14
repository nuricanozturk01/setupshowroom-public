package com.setupshowroom.setup;

import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "favorite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite implements Comparable<Favorite> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  private Setup setup;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps timestamps;

  @Override
  public int compareTo(final @NotNull Favorite o) {
    if (this.timestamps.getUpdatedAt() != null && o.timestamps.getUpdatedAt() != null) {
      return this.timestamps.getUpdatedAt().compareTo(o.timestamps.getUpdatedAt());
    }

    return this.timestamps.getCreatedAt().compareTo(o.timestamps.getCreatedAt());
  }
}

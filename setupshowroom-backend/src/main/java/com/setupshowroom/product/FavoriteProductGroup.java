package com.setupshowroom.product;

import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Builder
@Entity
@Table(name = "favorite_product_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProductGroup implements Comparable<FavoriteProductGroup> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps timestamps;

  @OneToMany(mappedBy = "favoriteProductGroup", cascade = CascadeType.ALL, orphanRemoval = true)
  private SortedSet<FavoriteProduct> products = new TreeSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Override
  public int compareTo(final @NotNull FavoriteProductGroup o) {
    if (this.timestamps.getUpdatedAt() != null && o.timestamps.getUpdatedAt() != null) {
      return this.timestamps.getUpdatedAt().compareTo(o.timestamps.getUpdatedAt());
    }

    return this.timestamps.getCreatedAt().compareTo(o.timestamps.getCreatedAt());
  }
}

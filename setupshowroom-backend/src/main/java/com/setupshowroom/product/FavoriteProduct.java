package com.setupshowroom.product;

import com.setupshowroom.shared.model.EmbeddedTimestamps;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Comparator;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "favorite_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteProduct implements Comparable<FavoriteProduct> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "name", length = 150, nullable = false)
  private String name;

  @Column(name = "url", length = 150, nullable = false)
  private String url;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @ManyToOne(fetch = FetchType.LAZY)
  private FavoriteProductGroup favoriteProductGroup;

  @Embedded private EmbeddedTimestamps timestamps;

  @Override
  public int compareTo(final @NotNull FavoriteProduct other) {
    return Objects.compare(this.name, other.name, Comparator.naturalOrder());
  }

  @Override
  public boolean equals(final @NotNull Object o) {
    if (!(o instanceof FavoriteProduct that)) {
      return false;
    }
    return Objects.equals(this.id, that.id)
        && Objects.equals(this.name, that.name)
        && Objects.equals(this.url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.name, this.url);
  }
}

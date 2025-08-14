package com.setupshowroom.comment;

import com.setupshowroom.setup.Setup;
import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment implements Comparable<Comment> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "setup_id", nullable = false)
  private Setup setup;

  @Column(nullable = false, length = 500)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @SortNatural
  private SortedSet<Comment> replies = new TreeSet<>();

  @OneToMany(
      mappedBy = "comment",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<CommentLike> likes = new HashSet<>();

  @Column(nullable = false)
  private int depth = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps embeddedTimestamps;

  @PrePersist
  @PreUpdate
  public void validateComment() {
    if (this.parent != null) {
      this.depth = this.parent.getDepth() + 1;
      if (this.depth > 2) {
        throw new IllegalStateException("Maximum comment depth exceeded");
      }

      if (!Objects.equals(this.setup.getId(), this.parent.getSetup().getId())) {
        throw new IllegalStateException("Comment must belong to same setup as parent");
      }
    }
  }

  @Override
  public int compareTo(final @NotNull Comment other) {
    return Objects.compare(
        this.embeddedTimestamps.getCreatedAt(),
        other.getEmbeddedTimestamps().getCreatedAt(),
        Instant::compareTo);
  }

  @Override
  public boolean equals(final @NotNull Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Comment comment)) {
      return false;
    }
    return this.id != null && Objects.equals(this.id, comment.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

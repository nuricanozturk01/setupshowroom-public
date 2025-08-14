package com.setupshowroom.setup;

import com.setupshowroom.comment.Comment;
import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.tag.Tag;
import com.setupshowroom.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "setup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Setup implements Comparable<Setup> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "title", nullable = false, length = 50)
  private String title;

  @Column(name = "description", nullable = false, length = 1_000)
  private String description;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  @SortNatural
  private SortedSet<SetupCategory> categories = new TreeSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @SortNatural
  private SortedSet<String> images = new TreeSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @SortNatural
  private SortedSet<String> videos = new TreeSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @OneToMany(
      mappedBy = "setup",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Comment> comments = new HashSet<>();

  @OneToMany(
      mappedBy = "setup",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Like> likes = new HashSet<>();

  @OneToMany(mappedBy = "setup", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Favorite> favorites = new HashSet<>();

  @ManyToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      fetch = FetchType.EAGER)
  @JoinTable(
      name = "setup_tag",
      joinColumns = @JoinColumn(name = "setup_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @SortNatural
  private SortedSet<Tag> tags = new TreeSet<>();

  @Embedded private EmbeddedTimestamps embeddedTimestamps;

  @Override
  public boolean equals(final @NotNull Object o) {
    if (!(o instanceof Setup setup)) {
      return false;
    }
    return Objects.equals(this.id, setup.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }

  @Override
  public int compareTo(final @NotNull Setup o) {
    if (this.embeddedTimestamps.getUpdatedAt() != null
        && o.embeddedTimestamps.getUpdatedAt() != null) {
      return this.embeddedTimestamps.getUpdatedAt().compareTo(o.embeddedTimestamps.getUpdatedAt());
    }

    return this.embeddedTimestamps.getCreatedAt().compareTo(o.embeddedTimestamps.getCreatedAt());
  }

  public void addComment(final @NotNull Comment savedComment) {
    this.comments.add(savedComment);
    savedComment.setSetup(this);
  }
}

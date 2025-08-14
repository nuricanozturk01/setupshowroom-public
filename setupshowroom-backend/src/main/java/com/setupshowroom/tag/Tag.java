package com.setupshowroom.tag;

import com.setupshowroom.setup.Setup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Builder
@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Comparable<Tag> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  private String name;

  @ManyToMany(mappedBy = "tags")
  private SortedSet<Setup> setups;

  @Override
  public int compareTo(final @NotNull Tag other) {
    return this.name.compareTo(other.name);
  }
}

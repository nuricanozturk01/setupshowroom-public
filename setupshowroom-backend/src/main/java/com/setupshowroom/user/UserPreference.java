package com.setupshowroom.user;

import com.setupshowroom.shared.model.EmbeddedTimestamps;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_preference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ElementCollection private List<String> preferredCategories;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps embeddedTimestamps;
}

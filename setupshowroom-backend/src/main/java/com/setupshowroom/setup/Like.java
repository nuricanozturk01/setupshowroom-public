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

@Entity
@Table(name = "like")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  private Setup setup;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps embeddedTimestamps;
}

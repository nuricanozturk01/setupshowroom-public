package com.setupshowroom.notification;

import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Comparable<Notification> {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "read", nullable = false)
  private boolean read;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(name = "action", nullable = false)
  private String action;

  @Column(name = "to", length = 26)
  private String to;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps timestamps;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Override
  public int compareTo(final @NotNull Notification o) {
    return this.timestamps.getCreatedAt().compareTo(o.timestamps.getCreatedAt());
  }
}

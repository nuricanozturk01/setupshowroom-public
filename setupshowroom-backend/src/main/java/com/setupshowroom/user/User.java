package com.setupshowroom.user;

import com.setupshowroom.comment.CommentLike;
import com.setupshowroom.notification.Notification;
import com.setupshowroom.product.FavoriteProductGroup;
import com.setupshowroom.report.Report;
import com.setupshowroom.setup.Favorite;
import com.setupshowroom.setup.Like;
import com.setupshowroom.setup.Setup;
import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.systeminfo.SystemRequirement;
import com.setupshowroom.user.profile.UserProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class User implements UserDetails {
  @Id
  @Column(name = "id", unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "username", unique = true)
  private String username;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "provider")
  private String provider;

  @Column(name = "provider_id")
  private String providerId;

  @Column(name = "hashed_password")
  private String hashedPassword;

  @Column(name = "salt")
  private String salt;

  @Column(name = "profession", length = 150)
  private String profession;

  @Column(name = "email_verified_code", length = 35)
  private String emailVerifiedCode;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
  private UserProfile userProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
  private UserPreference userPreference;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
  private SystemRequirement systemRequirement;

  @Column(name = "locked", nullable = false)
  private boolean locked;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SortedSet<Setup> setups = new TreeSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SortedSet<Favorite> favorites = new TreeSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SortedSet<Notification> notifications = new TreeSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Report> reports = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Like> likes = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CommentLike> commentLikes = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SortedSet<FavoriteProductGroup> productGroups = new TreeSet<>();

  @Embedded private EmbeddedTimestamps embeddedTimestamps;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return this.hashedPassword;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !this.locked;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public boolean equals(final @NotNull Object o) {
    if (!(o instanceof User user)) {
      return false;
    }
    return this.id.equals(user.id);
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
}

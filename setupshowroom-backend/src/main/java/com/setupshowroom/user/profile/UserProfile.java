package com.setupshowroom.user.profile;

import com.setupshowroom.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "website")
  private String website;

  @Column(name = "location")
  private String location;

  @Column(name = "bio")
  private String bio;

  @Column(name = "profile_picture_url")
  private String profilePictureUrl;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
}

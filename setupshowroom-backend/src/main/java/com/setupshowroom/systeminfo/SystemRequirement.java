package com.setupshowroom.systeminfo;

import com.setupshowroom.setup.SetupCategory;
import com.setupshowroom.shared.model.EmbeddedTimestamps;
import com.setupshowroom.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;

@Entity
@Table(name = "system_requirement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemRequirement {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Column(name = "cpu", length = 150)
  private String cpu;

  @Column(name = "gpu", length = 150)
  private String gpu;

  @Column(name = "ram", length = 150)
  private String ram;

  @Column(name = "storage", length = 150)
  private String storage;

  @Column(name = "motherboards", length = 150)
  private String motherboard;

  @Column(name = "psu", length = 150)
  private String psu;

  @Column(name = "case", length = 150)
  private String setupCase;

  @Column(name = "monitor", length = 150)
  private String monitor;

  @Column(name = "keyboard", length = 150)
  private String keyboard;

  @Column(name = "mouse", length = 150)
  private String mouse;

  @Column(name = "headset", length = 150)
  private String headset;

  @Column(name = "other", length = 1000)
  private String other;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  @SortNatural
  private SortedSet<SetupCategory> categories = new TreeSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @SortNatural
  private SortedSet<String> images = new TreeSet<>();

  @Embedded private EmbeddedTimestamps embeddedTimestamps;
}

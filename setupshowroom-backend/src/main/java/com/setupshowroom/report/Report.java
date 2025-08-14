package com.setupshowroom.report;

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

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
  @Id
  @Column(unique = true, length = 26, nullable = false, updatable = false)
  private String id;

  @Enumerated(EnumType.STRING)
  private ReportType type;

  @Column(name = "description", length = 500, nullable = false)
  private String description;

  @Column(name = "reported_item_id", length = 26, nullable = false)
  private String reportedItemId;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Embedded private EmbeddedTimestamps timestamps;

  @Override
  public String toString() {
    return "Report Type: "
        + this.type
        + "\nDescription: "
        + this.description
        + "\nReported Item ID: "
        + this.reportedItemId;
  }
}

package com.setupshowroom.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.setupshowroom.report.ReportType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportForm {
  private ReportType type;

  @Size(min = 10, max = 500, message = "Max 500 characters")
  private String description;

  @JsonProperty("reported_item_id")
  @Size(min = 26, max = 26, message = "Reported item ID must be 26 characters")
  private String reportedItemId;
}

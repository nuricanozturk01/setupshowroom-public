package com.setupshowroom.systeminfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemRequirementForm {
  @JsonProperty("case")
  private String setupCase;

  @Size(max = 80)
  private String cpu;

  @Size(max = 80)
  private String gpu;

  @Size(max = 80)
  private String ram;

  @Size(max = 80)
  private String storage;

  @Size(max = 80)
  private String motherboard;

  @Size(max = 80)
  private String psu;

  @Size(max = 80)
  private String monitor;

  @Size(max = 80)
  private String keyboard;

  @Size(max = 80)
  private String mouse;

  @Size(max = 80)
  private String headset;

  @Size(max = 255)
  private String other;

  private List<String> categories;

  @JsonProperty("existing_images")
  private List<String> existingImages;
}

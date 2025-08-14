package com.setupshowroom.systeminfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.setupshowroom.setup.SetupCategory;
import java.util.SortedSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemInfo {
  @JsonProperty("case")
  private String setupCase;

  private String cpu;
  private String gpu;
  private String ram;
  private String storage;
  private String motherboard;
  private String psu;
  private String monitor;
  private String keyboard;
  private String mouse;
  private String headset;
  private String other;
  private SortedSet<SetupCategory> categories;
  private SortedSet<String> images;
}

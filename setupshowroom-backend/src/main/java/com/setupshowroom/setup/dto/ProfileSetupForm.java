package com.setupshowroom.setup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.SortedSet;
import lombok.Data;

@Data
public class ProfileSetupForm {
  private String title;
  private String description;
  private SortedSet<String> categories;
  private SortedSet<String> tags;
  private String setupId;

  @JsonProperty("existing_images")
  private List<String> existingImages;
}

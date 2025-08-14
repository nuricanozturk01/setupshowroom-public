package com.setupshowroom.setup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SetupUpdateForm {
  @NotEmpty
  @Size(min = 2, max = 50)
  private String title;

  @Size(max = 1_001)
  private String description;

  @NotEmpty
  @Size(min = 1)
  private List<String> categories;

  @NotEmpty
  @Size(min = 1, max = 15)
  private List<String> tags;

  @JsonProperty("existing_images")
  private List<String> existingImages;

  @JsonProperty("existing_videos")
  private List<String> existingVideos;
}

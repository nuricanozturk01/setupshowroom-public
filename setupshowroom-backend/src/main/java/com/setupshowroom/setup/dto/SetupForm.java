package com.setupshowroom.setup.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.SortedSet;
import lombok.Data;

@Data
public class SetupForm {

  @NotEmpty
  @Size(min = 2, max = 50)
  private String title;

  @Size(max = 1_001)
  private String description;

  @NotEmpty
  @Size(min = 1)
  private SortedSet<String> categories;

  @NotEmpty
  @Size(min = 3, max = 15)
  private SortedSet<String> tags;

  private String setupId;
}

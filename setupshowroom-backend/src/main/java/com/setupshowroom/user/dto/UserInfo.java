package com.setupshowroom.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
  private String id;

  @JsonProperty("full_name")
  private String fullName;

  private String email;
  private String username;
  private String profession;
  private boolean enabled;
}

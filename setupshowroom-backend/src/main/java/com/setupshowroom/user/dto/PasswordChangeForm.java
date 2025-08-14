package com.setupshowroom.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class PasswordChangeForm {
  @JsonProperty("new_password")
  private String newPassword;
}

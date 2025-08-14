package com.setupshowroom.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RefreshTokenRequest {
  @JsonProperty("refresh_token")
  private String refreshToken;
}

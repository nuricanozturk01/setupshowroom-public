package com.setupshowroom.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserForm {

  @Size(max = 100)
  @JsonProperty("full_name")
  private String fullName;

  @NotEmpty
  @Size(min = 3, max = 80)
  private String username;

  @NotEmpty @Email private String email;

  @Size(max = 150)
  private String profession;
}

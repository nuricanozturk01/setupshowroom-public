package com.setupshowroom.user.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.setupshowroom.product.dto.FavoriteProductGroupInfo;
import com.setupshowroom.systeminfo.dto.SystemInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileInfo {
  private String id;

  @Size(max = 100)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("full_name")
  private String fullName;

  @Email
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String email;

  @NotEmpty
  @Size(min = 1, max = 80)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String username;

  @Size(max = 150)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String profession;

  @JsonProperty("picture")
  private String profilePictureUrl;

  @JsonProperty("system_info")
  private SystemInfo systemInfo;

  @JsonProperty("product_groups")
  private List<FavoriteProductGroupInfo> productGroups;
}

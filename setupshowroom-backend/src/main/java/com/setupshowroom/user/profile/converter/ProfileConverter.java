package com.setupshowroom.user.profile.converter;

import com.setupshowroom.product.dto.FavoriteProductGroupInfo;
import com.setupshowroom.systeminfo.dto.SystemInfo;
import com.setupshowroom.user.User;
import com.setupshowroom.user.profile.UserProfile;
import com.setupshowroom.user.profile.dto.ProfileInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProfileConverter {
  @Mappings({
    @Mapping(source = "user.fullName", target = "fullName"),
    @Mapping(source = "user.username", target = "username"),
    @Mapping(source = "user.email", target = "email"),
    @Mapping(source = "user.profession", target = "profession"),
    @Mapping(source = "user.userProfile.profilePictureUrl", target = "profilePictureUrl"),
    @Mapping(target = "productGroups", source = "productGroups"),
    @Mapping(target = "id", source = "user.id")
  })
  @NotNull
  ProfileInfo toProfileInfo(
      @NotNull UserProfile userProfile,
      @NotNull List<FavoriteProductGroupInfo> productGroups,
      @NotNull SystemInfo systemInfo,
      @NotNull User user);
}

package com.setupshowroom.user.converter;

import com.setupshowroom.user.User;
import com.setupshowroom.user.dto.UserInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
  UserInfo toUserInfo(User user);
}

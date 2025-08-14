package com.setupshowroom.notification.converter;

import com.setupshowroom.notification.Notification;
import com.setupshowroom.notification.dto.NotificationForm;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface NotificationConverter {
  @Mappings({
    @Mapping(target = "to", source = "to"),
    @Mapping(
        target = "id",
        expression = "java(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString())"),
  })
  @NotNull
  Notification toNotification(@NotNull NotificationForm form);
}

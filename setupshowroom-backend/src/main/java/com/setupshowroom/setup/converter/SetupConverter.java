package com.setupshowroom.setup.converter;

import com.setupshowroom.comment.Comment;
import com.setupshowroom.comment.dto.CommentInfo;
import com.setupshowroom.setup.Setup;
import com.setupshowroom.setup.dto.SetupForm;
import com.setupshowroom.tag.Tag;
import com.setupshowroom.user.dto.UserInfo;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SetupConverter {

  @Mappings({
    @Mapping(
        target = "id",
        expression = "java(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString())"),
    @Mapping(target = "tags", ignore = true),
  })
  Setup toSetup(@NotNull SetupForm setupForm);

  @Mappings({
    @Mapping(
        target = "id",
        expression = "java(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString())"),
    @Mapping(target = "name", source = "tag")
  })
  Tag toTag(@NotNull String tag);

  @Mappings({
    @Mapping(target = "author", source = "userInfo"),
    @Mapping(target = "id", source = "comment.id")
  })
  @NotNull
  CommentInfo toCommentInfo(@NotNull Comment comment, @NotNull UserInfo userInfo);
}

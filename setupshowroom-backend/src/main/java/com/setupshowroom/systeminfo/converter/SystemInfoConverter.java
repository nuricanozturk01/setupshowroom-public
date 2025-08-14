package com.setupshowroom.systeminfo.converter;

import com.setupshowroom.systeminfo.SystemRequirement;
import com.setupshowroom.systeminfo.dto.SystemInfo;
import com.setupshowroom.systeminfo.dto.SystemRequirementForm;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemInfoConverter {

  @NotNull
  SystemInfo toSystemInfo(@NotNull SystemRequirement systemRequirement);

  @NotNull
  SystemRequirement toSystemRequirement(@NotNull SystemRequirementForm systemRequirementForm);
}

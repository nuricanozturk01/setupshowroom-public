package com.setupshowroom.report.converter;

import com.setupshowroom.report.Report;
import com.setupshowroom.report.dto.ReportForm;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportConverter {
  @NotNull
  Report toReport(ReportForm reportForm);
}

package com.setupshowroom.report;

import com.setupshowroom.report.dto.ReportForm;
import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.shared.security.BearerTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
  private static final String JWT_TOKEN = "Bearer ";
  private final @NotNull ReportService reportService;
  private final @NotNull BearerTokenService bearerTokenService;

  @PostMapping
  public ResponseEntity<Response<?>> createReport(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authorization,
      @RequestBody @Valid final @NotNull ReportForm reportForm) {
    this.reportService.create(this.extractUserId(authorization), reportForm);
    return ResponseEntity.ok(
        Response.success("Report created successfully", null, HttpStatus.OK.value()));
  }

  private @NotNull String extractUserId(final @NotNull String authorization) {
    return this.bearerTokenService.extractUserId(authorization.substring(JWT_TOKEN.length()));
  }
}

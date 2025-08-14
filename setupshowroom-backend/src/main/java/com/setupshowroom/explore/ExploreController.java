package com.setupshowroom.explore;

import com.setupshowroom.setup.SetupFacade;
import com.setupshowroom.setup.dto.SetupInfo;
import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.shared.security.BearerTokenService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ExploreController {
  private final @NotNull SetupFacade setupFacade;
  private final @NotNull BearerTokenService bearerTokenService;

  @GetMapping("/explore/setups")
  public ResponseEntity<Response<?>> exploreSetups(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PageableDefault final @NotNull Pageable pageable) {
    final var userId = this.extractUserId(authHeader);
    final List<SetupInfo> setups = this.setupFacade.exploreSetups(userId, pageable);
    return ResponseEntity.ok(Response.success("retrieved", setups, HttpStatus.OK.value()));
  }

  @GetMapping("/feed")
  public ResponseEntity<Response<?>> feed(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authHeader,
      @PageableDefault final @NotNull Pageable pageable) {
    final var userId = this.extractUserId(authHeader);
    final List<SetupInfo> setups = this.setupFacade.feedSetups(userId, pageable);

    return ResponseEntity.ok(Response.success("retrieved", setups, HttpStatus.OK.value()));
  }

  private @NotNull String extractUserId(@NotNull final String token) {
    return this.bearerTokenService.extractUserId(token.substring("Bearer ".length()));
  }
}

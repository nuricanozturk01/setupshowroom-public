package com.setupshowroom.card;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/card")
@RequiredArgsConstructor
public class CardController {
  private static final String SVG_MEDIA_TYPE = "image/svg+xml";
  private final @NotNull SetupCardService setupCardService;

  @GetMapping(value = "/user/{userId}/sys-card", produces = SVG_MEDIA_TYPE)
  public ResponseEntity<String> getUserSysInfoCard(
      @PathVariable final @NotNull String userId, @RequestParam final @NotNull String type) {
    final var card = this.setupCardService.getUserSysInfoCard(userId, type);

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(SVG_MEDIA_TYPE))
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .body(card);
  }
}

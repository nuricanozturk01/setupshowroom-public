package com.setupshowroom.card;

import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.shared.security.BearerTokenService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardApiController {
  private final @NotNull SetupCardService setupCardService;
  private final @NotNull BearerTokenService bearerTokenService;

  @GetMapping("/list")
  public Response<List<String>> getCardList(
      @RequestHeader(HttpHeaders.AUTHORIZATION) final @NotNull String authorizationHeader) {
    final List<String> cardList =
        this.setupCardService.getCardList(this.getUserId(authorizationHeader));

    return Response.success("success", cardList, HttpStatus.OK.value());
  }

  private @NotNull String getUserId(final @NotNull String authHeader) {
    return this.bearerTokenService.extractUserId(authHeader.substring("Bearer ".length()));
  }
}

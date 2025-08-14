package com.setupshowroom.shared.security.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

@UtilityClass
public final class TokenVerifier {
  public static final String BEARER_PREFIX = "Bearer ";

  public static boolean isBearerToken(final String token) {
    return StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX);
  }

  public static String getJwtToken(final @NotNull String authorizationHeader) {
    return authorizationHeader.substring(BEARER_PREFIX.length());
  }
}

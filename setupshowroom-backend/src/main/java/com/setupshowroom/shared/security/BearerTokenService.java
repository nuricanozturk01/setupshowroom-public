package com.setupshowroom.shared.security;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.shared.exceptionhandler.exception.TokenExpiredException;
import com.setupshowroom.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BearerTokenService {
  private static final int ACCESS_EXPIRATION_HOUR = 24;
  private static final int REFRESH_EXPIRATION_HOUR = 48;
  private static final String ISSUER = "setupshowroom.com";
  private static final String EMAIL = "email";

  @Value("${app.jwt.secret}")
  private String secret;

  private SecretKey getSigningKey() {
    final byte[] keyBytes = java.util.Base64.getDecoder().decode(this.secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public @NotNull String extractUserId(final @NotNull String token) {
    try {
      return this.extractClaim(token, Claims::getSubject);
    } catch (final JwtException e) {
      throw new TokenExpiredException("invalidToken");
    }
  }

  public @NotNull <T> T extractClaim(
      final @NotNull String token, final @NotNull Function<Claims, T> claimsResolver) {
    final Claims claims = this.extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(final @NotNull String token) {
    return Jwts.parser()
        .verifyWith(this.getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public @NotNull String generateToken(final @NotNull User user) {
    final var claims = Map.of(EMAIL, user.getEmail());
    return this.createToken(claims, user.getId(), ACCESS_EXPIRATION_HOUR);
  }

  public @NotNull String generateRefreshToken(final @NotNull User user) {
    final var claims = Map.of(EMAIL, user.getEmail());
    return this.createToken(claims, user.getId(), REFRESH_EXPIRATION_HOUR);
  }

  private @NotNull String createToken(
      final @NotNull Map<String, String> claims,
      final @NotNull String subject,
      final int expiration) {
    final long currentTimeMillis = System.currentTimeMillis();

    final Date tokenIssuedAt = new Date(currentTimeMillis);

    final Date accessTokenExpiresAt = DateUtils.addHours(new Date(currentTimeMillis), expiration);

    return Jwts.builder()
        .header()
        .type(OAuth2AccessToken.TokenType.BEARER.getValue())
        .and()
        .id(UlidCreator.getUlid().toString())
        .subject(subject)
        .issuer(ISSUER)
        .issuedAt(tokenIssuedAt)
        .expiration(accessTokenExpiresAt)
        .signWith(this.getSigningKey())
        .claims(claims)
        .compact();
  }

  public void verifyAndValidate(final @NotNull String jwt) {
    try {
      Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims(jwt);
    } catch (final ExpiredJwtException e) {
      log.info("Token expired: {}", e.getMessage());
    } catch (final JwtException e) {
      log.info("Invalid token: {}", e.getMessage());
    }
  }
}

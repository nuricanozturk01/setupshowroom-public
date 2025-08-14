package com.setupshowroom.shared.security;

import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.shared.security.utils.TokenVerifier;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Configuration
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final BearerTokenService bearerTokenService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      final @NotNull HttpServletRequest request,
      final @NotNull HttpServletResponse response,
      final @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (!TokenVerifier.isBearerToken(authorizationHeader)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = TokenVerifier.getJwtToken(authorizationHeader);

    this.bearerTokenService.verifyAndValidate(jwt);

    final String userId = this.bearerTokenService.extractUserId(jwt);
    final User user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    final var authentication =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}

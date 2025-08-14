package com.setupshowroom.shared.security;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import com.setupshowroom.user.profile.UserProfile;
import com.setupshowroom.user.profile.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  private final BearerTokenService bearerTokenService;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;

  @Value("${app.frontend.base-url}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationSuccess(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Authentication authentication)
      throws IOException {
    final OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    final String emailOrUsername =
        oauth2User.getAttributes().get("email") != null
            ? (String) oauth2User.getAttributes().get("email")
            : (String) oauth2User.getAttributes().get("login");

    final var userOpt = this.userRepository.findByUsernameOrEmail(emailOrUsername);
    User user = userOpt.orElse(null);
    if (userOpt.isEmpty()) {
      user = this.createUserFromGoogle(oauth2User);
    }

    final String redirectUrl =
        UriComponentsBuilder.fromUriString("%s/login".formatted(this.frontendBaseUrl))
            .queryParam("token", this.bearerTokenService.generateToken(user))
            .queryParam("refresh_token", this.bearerTokenService.generateRefreshToken(user))
            .build()
            .toUriString();

    response.sendRedirect(redirectUrl);
  }

  private @NotNull User createUserFromGoogle(final @NotNull OAuth2User oauth2User) {
    final String sub = (String) oauth2User.getAttributes().get("sub");
    final String fullName = (String) oauth2User.getAttributes().get("name");
    final String picture = (String) oauth2User.getAttributes().get("picture");
    final String email = (String) oauth2User.getAttributes().get("email");

    final String emailVerificationCode = RandomStringUtils.secure().nextAlphanumeric(35);

    final User user =
        User.builder()
            .id(UlidCreator.getUlid().toString())
            .providerId(sub)
            .fullName(fullName)
            .username(email)
            .email(email)
            .emailVerifiedCode(emailVerificationCode)
            .enabled(true)
            .build();

    final User savedUser = this.userRepository.save(user);

    final UserProfile userProfile =
        UserProfile.builder()
            .id(UlidCreator.getUlid().toString())
            .profilePictureUrl(picture)
            .user(savedUser)
            .build();

    savedUser.setUserProfile(userProfile);

    this.userProfileRepository.save(userProfile);

    log.warn(
        "User has been registered via oauth2 register: {} - {}",
        savedUser.getUsername(),
        savedUser.getEmail());

    return savedUser;
  }
}

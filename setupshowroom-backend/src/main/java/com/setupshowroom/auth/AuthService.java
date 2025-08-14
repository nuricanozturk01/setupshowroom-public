package com.setupshowroom.auth;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.auth.dto.AuthResponse;
import com.setupshowroom.auth.dto.LoginForm;
import com.setupshowroom.auth.dto.RegisterForm;
import com.setupshowroom.shared.exceptionhandler.exception.AccessNotAllowedException;
import com.setupshowroom.shared.exceptionhandler.exception.UnAuthorizedException;
import com.setupshowroom.shared.security.BearerTokenService;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import com.setupshowroom.user.profile.UserProfile;
import com.setupshowroom.user.profile.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final BearerTokenService bearerTokenService;

  public @NotNull AuthResponse generateToken(final @NotNull User user) {
    final var token = this.bearerTokenService.generateToken(user);
    final var refreshToken = this.bearerTokenService.generateRefreshToken(user);

    return new AuthResponse(token, refreshToken);
  }

  public @NotNull User register(final @NotNull RegisterForm registerForm) {
    final var emailExists =
        this.userRepository.existsByEmailAndLockedFalseAndEnabledTrue(registerForm.email());

    final var usernameExists =
        this.userRepository.existsByUsernameAndLockedFalseAndEnabledTrue(registerForm.username());

    if (emailExists || usernameExists) {
      throw new AccessNotAllowedException("invalidCredentials");
    }

    final var salt = RandomStringUtils.secure().nextAlphanumeric(16);
    final var emailVerificationCode = RandomStringUtils.secure().nextAlphanumeric(35);
    final var password = DigestUtils.sha256Hex(salt + registerForm.password());

    final User user =
        User.builder()
            .id(UlidCreator.getUlid().toString())
            .email(registerForm.email())
            .emailVerifiedCode(emailVerificationCode)
            .username(registerForm.username())
            .salt(salt)
            .enabled(true)
            .hashedPassword(password)
            .build();

    final var savedUser = this.userRepository.save(user);

    final var userProfile =
        UserProfile.builder().id(UlidCreator.getUlid().toString()).user(savedUser).build();

    savedUser.setUserProfile(userProfile);

    this.userProfileRepository.save(userProfile);

    log.warn(
        "User has been registered via classic register: {} - {}",
        savedUser.getUsername(),
        savedUser.getEmail());

    return savedUser;
  }

  public @NotNull User login(final @NotNull LoginForm loginForm) {
    final var user = this.userRepository.findByUsernameOrEmail(loginForm.emailOrUsername());
    if (user.isEmpty()) {
      throw new UnAuthorizedException("userNotFound");
    }

    if (!user.get().isEnabled()) {
      throw new UnAuthorizedException("userNotEnabled");
    }

    if (user.get().isLocked()) {
      throw new UnAuthorizedException("userLocked");
    }

    final var hashedPassword = DigestUtils.sha256Hex(user.get().getSalt() + loginForm.password());
    final var usePasswordHash = user.get().getHashedPassword();

    if (!usePasswordHash.equals(hashedPassword)) {
      throw new UnAuthorizedException("passwordNotMatch");
    }

    return user.get();
  }

  public @NotNull AuthResponse refreshAccessToken(final @NotNull String refreshToken) {
    final var userId = this.bearerTokenService.extractUserId(refreshToken);
    final var user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new UnAuthorizedException("userNotFound"));

    final var accessToken = this.bearerTokenService.generateToken(user);
    final var newRefreshToken = this.bearerTokenService.generateRefreshToken(user);

    return new AuthResponse(accessToken, newRefreshToken);
  }
}

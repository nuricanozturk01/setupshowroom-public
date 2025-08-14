package com.setupshowroom.auth;

import com.setupshowroom.auth.dto.AuthResponse;
import com.setupshowroom.auth.dto.LoginForm;
import com.setupshowroom.auth.dto.RefreshTokenRequest;
import com.setupshowroom.auth.dto.RegisterForm;
import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.user.User;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
  private final @NotNull AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<Response<AuthResponse>> register(
      @RequestBody final @NotNull RegisterForm registerForm) {
    final User user = this.authService.register(registerForm);
    final AuthResponse authResponse = this.authService.generateToken(user);

    final var response =
        Response.success("User registered successfully", authResponse, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<Response<AuthResponse>> login(
      @RequestBody final @NotNull LoginForm loginForm) {
    final User user = this.authService.login(loginForm);
    final AuthResponse authResponse = this.authService.generateToken(user);

    final var response =
        Response.success("User login successfully", authResponse, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<Response<AuthResponse>> refreshAccessToken(
      @RequestBody final @NotNull RefreshTokenRequest refreshTokenRequest) {

    final AuthResponse authResponse =
        this.authService.refreshAccessToken(refreshTokenRequest.getRefreshToken());

    final var response =
        Response.success(
            "Access token refreshed successfully", authResponse, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  // ...
}

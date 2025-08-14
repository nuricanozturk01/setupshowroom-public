package com.setupshowroom.shared.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
  private static final long MAX_AGE = 3600L;

  private final @NotNull CustomAuthenticationSuccessHandler successHandler;
  private final @NotNull JwtAuthenticationFilter jwtAuthenticationFilter;
  private final @NotNull CustomAuthenticationEntryPoint authenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(final @NotNull HttpSecurity http)
      throws Exception {
    return http.exceptionHandling(c -> c.authenticationEntryPoint(this.authenticationEntryPoint))
        .cors(this::cors)
        .headers(f -> f.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2Login(oauth2 -> oauth2.successHandler(this.successHandler))
        .authorizeHttpRequests(this::authorizeHttpRequests)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      final @NotNull AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public @NotNull HttpFirewall defaultHttpFirewall() {
    return new DefaultHttpFirewall();
  }

  @Bean
  public @NotNull WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.httpFirewall(this.defaultHttpFirewall());
  }

  @Bean
  public @NotNull PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private void cors(final @NotNull CorsConfigurer<HttpSecurity> corsConfigurer) {
    corsConfigurer.configurationSource(this.corsConfigurationSource());
  }

  @NotNull
  public CorsConfigurationSource corsConfigurationSource() {
    final var configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowCredentials(false);
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("*"));
    configuration.setMaxAge(MAX_AGE);
    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private void authorizeHttpRequests(
      final @NotNull AuthorizeHttpRequestsConfigurer<HttpSecurity>
                  .AuthorizationManagerRequestMatcherRegistry
              auth) {
    auth.requestMatchers(
            "/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/login",
            "/login/oauth2/code/*",
            "/oauth2/authorization/*",
            "/login/success",
            "/login/failure",
            "/favicon.ico",
            "/public/notification/stream",
            "/logout/success")
        .permitAll();

    auth.requestMatchers("/actuator/**").permitAll();
    auth.requestMatchers("/public/**").permitAll();
    auth.anyRequest().authenticated();
  }
}

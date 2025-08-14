package com.setupshowroom.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.setupshowroom.shared.dto.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Override
  public void commence(
      final HttpServletRequest httpServletRequest,
      final HttpServletResponse httpServletResponse,
      final AuthenticationException authenticationException)
      throws IOException {

    httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
    httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

    final Response<String> response =
        Response.error("unauthorized", HttpStatus.UNAUTHORIZED.value());

    final String responseBody =
        OBJECT_MAPPER.writer(DateFormat.getDateInstance()).writeValueAsString(response);

    httpServletResponse.getOutputStream().write(responseBody.getBytes());
  }
}

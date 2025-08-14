package com.setupshowroom.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(String token, @JsonProperty("refresh_token") String refreshToken) {}

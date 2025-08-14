package com.setupshowroom.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginForm(
    @JsonProperty("username_or_email") String emailOrUsername, String password) {}

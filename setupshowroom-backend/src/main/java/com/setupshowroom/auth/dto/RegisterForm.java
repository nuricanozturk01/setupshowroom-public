package com.setupshowroom.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterForm(
    @NotEmpty @Size(min = 3, max = 80) String username,
    @NotEmpty @Email String email,
    @NotEmpty @Size(min = 6, max = 80) String password) {}

package com.setupshowroom.product.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UrlValidator implements ConstraintValidator<Url, String> {
  @Override
  public boolean isValid(
      final @NotNull String value, final @NotNull ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return false;
    }

    try {
      final URL url = URI.create(value).toURL();
      final String protocol = url.getProtocol();
      return protocol.equals("http") || protocol.equals("https");
    } catch (final MalformedURLException e) {
      return false;
    }
  }
}

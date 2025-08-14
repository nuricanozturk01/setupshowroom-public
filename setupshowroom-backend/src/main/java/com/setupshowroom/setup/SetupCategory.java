package com.setupshowroom.setup;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public enum SetupCategory {
  ROOM(1),
  WORKSPACE(2),
  RGB(3),
  MINIMALIST(4),
  PC(5),
  DEVELOPMENT(6),
  PRODUCTIVITY(7),
  GAMING(8);

  private final int value;

  SetupCategory(final @NotNull int value) {
    this.value = value;
  }

  public static SetupCategory fromValue(final @NotNull int value) {
    for (final @NotNull SetupCategory category : values()) {
      if (category.value == value) {
        return category;
      }
    }
    throw new IllegalArgumentException("Invalid category value: " + value);
  }
}

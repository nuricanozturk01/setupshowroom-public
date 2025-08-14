package com.setupshowroom.shared.storage.common;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class RelativePath {
  private final @NotNull String path;
  private final @NotNull String fileName;

  public RelativePath(final @NotNull String path) {
    this.path = path;
    final String[] segments = path.split("/", -1);

    if (segments.length == 0) {
      this.fileName = path;
    } else {
      this.fileName = segments[segments.length - 1];
    }
  }
}

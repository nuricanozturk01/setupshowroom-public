package com.setupshowroom.shared.storage.common;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Getter
public class StoragePath {
  private final String repoId;
  private final RelativePath relativePath;
  private final String path;

  public StoragePath(final @NotNull String repoId, final @NotNull RelativePath relativePath) {
    this.repoId = repoId;
    this.relativePath = relativePath;

    if (relativePath.getPath().isEmpty()) {
      this.path = repoId;
    } else {
      this.path = repoId + "/" + StringUtils.removeStart(relativePath.getPath(), '/');
    }
  }

  public static @NotNull StoragePath of(
      final @NotNull String repoId, final @NotNull String relativePath) {
    return new StoragePath(repoId, new RelativePath(relativePath));
  }
}

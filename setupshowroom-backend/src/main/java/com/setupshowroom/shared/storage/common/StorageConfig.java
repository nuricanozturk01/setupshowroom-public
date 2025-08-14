package com.setupshowroom.shared.storage.common;

import com.setupshowroom.shared.storage.s3.S3StorageBackendConfigProps;
import com.setupshowroom.shared.storage.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {
  private final @NotNull S3StorageBackendConfigProps s3StorageBackendConfigProps;

  @Bean
  public @NonNull S3StorageService storageStrategy() {
    return new S3StorageService(this.s3StorageBackendConfigProps);
  }
}

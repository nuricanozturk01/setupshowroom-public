package com.setupshowroom.shared.storage.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.storage.s3")
public class S3StorageBackendConfigProps {
  private String endpoint;
  private String bucket;
  private String basePath;
  private String trashPath;
  private String accessKey;
  private String secretKey;
}

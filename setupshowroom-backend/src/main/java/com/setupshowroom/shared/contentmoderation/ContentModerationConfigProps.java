package com.setupshowroom.shared.contentmoderation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.sightengine")
public class ContentModerationConfigProps {
  private String models;
  private String apiUser;
  private String apiSecret;
  private boolean active;
}

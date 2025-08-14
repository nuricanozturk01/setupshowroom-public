package com.setupshowroom;

import com.setupshowroom.shared.contentmoderation.ContentModerationConfigProps;
import com.setupshowroom.shared.storage.s3.S3StorageBackendConfigProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
  S3StorageBackendConfigProps.class,
  ContentModerationConfigProps.class
})
@EnableJpaRepositories
@EnableFeignClients
public class SetupShowroomBackendApplication {
  public SetupShowroomBackendApplication() {
    log.warn("Setup Showroom Backend Application started");
  }

  public static void main(final String[] args) {
    SpringApplication.run(SetupShowroomBackendApplication.class, args);
  }
}

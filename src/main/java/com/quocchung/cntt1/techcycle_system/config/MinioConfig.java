package com.quocchung.cntt1.techcycle_system.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Bean
  @ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
  public MinioClient minioClient(MinioProperties minioProperties) {
    return MinioClient.builder()
        .endpoint(minioProperties.getEndpoint())
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .build();
  }
}

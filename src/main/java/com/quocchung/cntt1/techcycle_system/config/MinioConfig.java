package com.quocchung.cntt1.techcycle_system.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinioConfig {

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
  public MinioClient minioClient(MinioProperties minioProperties) {
    return MinioClient.builder()
        .endpoint(minioProperties.getEndpoint())       // http://minio:9000
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .build();
  }

  @Bean("publicMinioClient")
  @ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
  public MinioClient publicMinioClient(MinioProperties minioProperties) {
    return MinioClient.builder()
        .endpoint(minioProperties.getPublicEndpoint()) // http://62.171.174.204:9000
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .build();
  }
}

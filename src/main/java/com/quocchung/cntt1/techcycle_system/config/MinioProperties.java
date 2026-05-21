package com.quocchung.cntt1.techcycle_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
  private boolean enabled = true;
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucketName = "techcycle";
  private String publicEndpoint;
}

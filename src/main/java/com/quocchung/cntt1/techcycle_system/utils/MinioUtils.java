package com.quocchung.cntt1.techcycle_system.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioUtils {

  private static String publicEndpoint;

  @Value("${minio.public-endpoint:}")
  public void setPublicEndpoint(String endpoint) {
    MinioUtils.publicEndpoint = endpoint;
  }

  public static String buildPublicUrl(String objectKey) {
    String base = publicEndpoint;
    if (base == null || base.isBlank()) {
      base = "http://localhost:9000";
    }
    return base + "/" + objectKey;
  }
}

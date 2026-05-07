package com.quocchung.cntt1.techcycle_system.dtos.request.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlResponse {
  private String presignedUrl;
  private String objectKey;
  private String publicUrl;
  private Long expiresInSeconds;
}

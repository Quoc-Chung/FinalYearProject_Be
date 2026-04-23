package com.quocchung.cntt1.techcycle_system.dtos.response.Minio;

import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageUploadResponse {
  private String objectKey;
  private String url;
  private MediaType mediaType;
  private String mimeType;
  private String fileName;
  private Long fileSize;
}

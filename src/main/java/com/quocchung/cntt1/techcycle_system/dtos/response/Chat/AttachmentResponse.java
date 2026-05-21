package com.quocchung.cntt1.techcycle_system.dtos.response.Chat;

import com.quocchung.cntt1.techcycle_system.model.MessageAttachment;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

  private Long attachmentId;
  private MediaType mediaType;
  private String objectKey;
  private String url;
  private String mimeType;
  private Long fileSize;
  private Integer durationSeconds;

  public static AttachmentResponse fromEntity(MessageAttachment attachment) {
    return AttachmentResponse.builder()
        .attachmentId(attachment.getAttachmentId())
        .mediaType(attachment.getMediaType())
        .objectKey(attachment.getObjectKey())
        .url(attachment.getObjectKey())
        .mimeType(attachment.getMimeType())
        .fileSize(attachment.getFileSize())
        .durationSeconds(attachment.getDurationSeconds())
        .build();
  }
}

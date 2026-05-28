package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import com.quocchung.cntt1.techcycle_system.utils.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

  @NotNull
  private Long conversationId;

  @NotNull
  private MessageType messageType;

  private String content;

  private List<AttachmentInfo> attachments;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttachmentInfo {
    private String objectKey;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private Integer durationSeconds;
    private MediaType mediaType;
  }
}

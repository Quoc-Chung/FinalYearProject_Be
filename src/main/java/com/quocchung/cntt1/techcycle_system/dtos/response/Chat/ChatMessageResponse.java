package com.quocchung.cntt1.techcycle_system.dtos.response.Chat;

import com.quocchung.cntt1.techcycle_system.model.Message;
import com.quocchung.cntt1.techcycle_system.utils.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

  private Long messageId;
  private Long conversationId;
  private Long senderId;
  private String senderName;
  private String senderAvatar;
  private MessageType messageType;
  private String content;
  private List<AttachmentResponse> attachments;
  private Boolean isRead;
  private LocalDateTime createdAt;

  public static ChatMessageResponse fromEntity(Message message) {
    return ChatMessageResponse.builder()
        .messageId(message.getMessageId())
        .conversationId(message.getConversation().getConversationId())
        .senderId(message.getSender().getUserId())
        .senderName(message.getSender().getFullName())
        .senderAvatar(message.getSender().getAvatarUrl())
        .messageType(message.getMessageType())
        .content(message.getContent())
        .isRead(message.getIsRead())
        .createdAt(message.getCreatedAt())
        .build();
  }
  public static ChatMessageResponse fromEntityWithAttachments(
      Message message,
      List<AttachmentResponse> attachments
  ) {
    ChatMessageResponse response = fromEntity(message);
    response.setAttachments(attachments);
    return response;
  }
}

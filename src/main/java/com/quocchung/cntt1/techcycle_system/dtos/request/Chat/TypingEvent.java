package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingEvent {
  private Long conversationId;
  private Long userId;
  private String userName;
  private String avatarURL;
  private boolean isTyping;
}

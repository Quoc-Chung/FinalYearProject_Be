package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingNotification {
  private boolean isTyping;
}

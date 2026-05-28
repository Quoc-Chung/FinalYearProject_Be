package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadReceiptEvent {
  private Long conversationId;
  private Long userId;   // ID của người dùng đã đọc
  private String username;
  private String avatarURL;
  private long timestamp;

}

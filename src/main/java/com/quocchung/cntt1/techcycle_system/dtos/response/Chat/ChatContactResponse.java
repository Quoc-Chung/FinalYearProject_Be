package com.quocchung.cntt1.techcycle_system.dtos.response.Chat;

import com.quocchung.cntt1.techcycle_system.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactResponse {

  private Long userId;
  private String fullName;
  private String email;
  private String avatarUrl;
  private Boolean isOnline;
  private LocalDateTime lastMessageAt;

  public static ChatContactResponse fromUser(User user, boolean isOnline, LocalDateTime lastMessageAt) {
    return ChatContactResponse.builder()
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .avatarUrl(user.getAvatarUrl())
        .isOnline(isOnline)
        .lastMessageAt(lastMessageAt)
        .build();
  }
}

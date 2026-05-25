package com.quocchung.cntt1.techcycle_system.dtos.response.Notification;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

  private Long notificationId;
  private NotificationType type;
  private String title;
  private String content;
  private String targetUrl;
  private Map<String, Object> data;
  private Boolean isRead;
  private LocalDateTime createdAt;

  private ActorInfo actor;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ActorInfo {
    private Long actorId;
    private String actorName;
    private String actorAvatar;
  }
}

package com.quocchung.cntt1.techcycle_system.dtos.response.Notification;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingResponse {

  private Long prefId;
  private NotificationType type;
  private Boolean enabled;
}

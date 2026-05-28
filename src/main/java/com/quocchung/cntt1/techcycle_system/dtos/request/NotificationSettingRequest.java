package com.quocchung.cntt1.techcycle_system.dtos.request;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingRequest {

  private NotificationType type;
  private Boolean enabled;
}

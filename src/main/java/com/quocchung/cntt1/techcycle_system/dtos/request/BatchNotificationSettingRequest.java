package com.quocchung.cntt1.techcycle_system.dtos.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchNotificationSettingRequest {

  @NotNull(message = "Settings list is required")
  private List<NotificationSettingRequest> settings;
}

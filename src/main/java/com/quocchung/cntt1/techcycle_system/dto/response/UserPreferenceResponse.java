package com.quocchung.cntt1.techcycle_system.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceResponse {
  
  private Long prefId;
  private Long userId;
  private String preferredProvince;
  private String interests;
  private String notificationSettings;
  private Boolean welcomeCompleted;
}

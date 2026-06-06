package com.quocchung.cntt1.techcycle_system.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceRequest {
  
  @Size(max = 255, message = "Province must not exceed 255 characters")
  private String preferredProvince;
  
  @Size(max = 500, message = "Interests must not exceed 500 characters")
  private String interests; // comma separated: buy,sell,rating,follow
  
  private String notificationSettings; // JSON string
  
  private Boolean welcomeCompleted;
}

package com.quocchung.cntt1.techcycle_system.dtos.response.Address;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {
  private Long addressId;
  private Long userId;
  private String province;
  private String ward;
  private String addressDetail;
  private String addressLine;
  private Boolean isDefault;
  private LocalDateTime createdAt;
}

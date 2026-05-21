package com.quocchung.cntt1.techcycle_system.dtos.request.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAddressRequest {

  @NotBlank(message = "Province is required")
  @Size(max = 255, message = "Province must not exceed 255 characters")
  private String province;

  @Size(max = 100, message = "Ward must not exceed 100 characters")
  private String ward;

  @NotBlank(message = "Address detail is required")
  @Size(max = 1000, message = "Address detail must not exceed 1000 characters")
  private String addressDetail;

  @Size(max = 5000, message = "Address line must not exceed 5000 characters")
  private String addressLine;

  private Boolean isDefault;
}

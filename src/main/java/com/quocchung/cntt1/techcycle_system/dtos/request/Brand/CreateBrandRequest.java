package com.quocchung.cntt1.techcycle_system.dtos.request.Brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandRequest {
  @NotNull(message = "categoryId is required")
  private Long categoryId;

  @NotBlank(message = "name is required")
  private String name;

  private Boolean isActive;
}

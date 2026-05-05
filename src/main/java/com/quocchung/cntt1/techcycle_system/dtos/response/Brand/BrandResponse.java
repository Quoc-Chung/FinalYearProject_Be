package com.quocchung.cntt1.techcycle_system.dtos.response.Brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
  private Long brandId;
  private Long categoryId;
  private String categoryName;
  private String name;
  private String logoUrl;
  private Boolean isActive;
}

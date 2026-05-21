package com.quocchung.cntt1.techcycle_system.dtos.response.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
  private Long categoryId;
  private Long parentId;
  private String parentName;
  private String name;
  private String iconUrl;
  private Boolean isActive;
}

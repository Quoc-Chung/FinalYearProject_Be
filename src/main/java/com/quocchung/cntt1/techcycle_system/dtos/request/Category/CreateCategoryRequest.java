package com.quocchung.cntt1.techcycle_system.dtos.request.Category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
  private Long parentId;
  @NotBlank(message = "name is required")
  private String name;
  private Boolean isActive;

}

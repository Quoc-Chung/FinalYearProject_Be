package com.quocchung.cntt1.techcycle_system.dtos.request.PostCollection;

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
public class PostCollectionRequest {

  @NotBlank(message = "Collection name must not be blank")
  @Size(max = 100, message = "Collection name must not exceed 100 characters")
  private String name;

  private Boolean isDefault;
}

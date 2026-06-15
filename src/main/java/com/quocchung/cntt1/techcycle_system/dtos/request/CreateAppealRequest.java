package com.quocchung.cntt1.techcycle_system.dtos.request;

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
public class CreateAppealRequest {
  @NotNull(message = "Post ID is required")
  private Long postId;

  @NotBlank(message = "Reason is required")
  private String reason;
}
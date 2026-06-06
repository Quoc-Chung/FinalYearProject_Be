package com.quocchung.cntt1.techcycle_system.dtos.request.Transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkSoldRequest {
  @NotNull
  private Long postId;
  @NotNull
  private Long buyerId;
}

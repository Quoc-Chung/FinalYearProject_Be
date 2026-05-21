package com.quocchung.cntt1.techcycle_system.dtos.request.Reaction;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactionRequest {

  @NotNull(message = "Loại cảm xúc không được để trống")
  private ReactionType reactionType;
}

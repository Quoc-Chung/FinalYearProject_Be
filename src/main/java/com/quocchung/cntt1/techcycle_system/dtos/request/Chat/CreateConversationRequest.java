package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

  @NotNull
  private Long postId;

  private List<Long> participantIds;

  private String initialMessage;
}

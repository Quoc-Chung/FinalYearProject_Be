package com.quocchung.cntt1.techcycle_system.dtos.response.Reaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCountItem {
  private Long postId;
  private Long totalReactions;
  private String topReaction;
}

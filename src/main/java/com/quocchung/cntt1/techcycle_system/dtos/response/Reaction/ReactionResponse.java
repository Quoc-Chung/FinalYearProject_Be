package com.quocchung.cntt1.techcycle_system.dtos.response.Reaction;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReactionResponse {

  private Long reactionId;
  private Long postId;
  private Long userId;
  private ReactionType reactionType;
  private LocalDateTime createdAt;
  private Map<ReactionType, Long> reactionCounts;
  private Long totalReactions;
  private ReactionType userReactionType;
}

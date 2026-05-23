package com.quocchung.cntt1.techcycle_system.dtos.request.Chat;

import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.Conversation;
import com.quocchung.cntt1.techcycle_system.model.ConversationParticipant;
import com.quocchung.cntt1.techcycle_system.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

  private Long conversationId;
  private UserSummary creator;
  private List<UserSummary> participants;
  private ChatMessageResponse lastMessage;
  private LocalDateTime lastMessageAt;
  private LocalDateTime createdAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserSummary {
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private Boolean isOnline;
  }

  public static ConversationResponse fromEntity(
      Conversation conversation,
      List<ConversationParticipant> participants,
      ChatMessageResponse lastMessage
  ) {
    List<UserSummary> participantSummaries = participants.stream()
        .map(p -> UserSummary.builder()
            .userId(p.getUser().getUserId())
            .fullName(p.getUser().getFullName())
            .avatarUrl(p.getUser().getAvatarUrl())
            .isOnline(false)
            .build())
        .toList();

    UserSummary creatorSummary = null;
    if (conversation.getCreatedBy() != null) {
      User creator = conversation.getCreatedBy();
      creatorSummary = UserSummary.builder()
          .userId(creator.getUserId())
          .fullName(creator.getFullName())
          .avatarUrl(creator.getAvatarUrl())
          .isOnline(false)
          .build();
    }

    return ConversationResponse.builder()
        .conversationId(conversation.getConversationId())
        .creator(creatorSummary)
        .participants(participantSummaries)
        .lastMessage(lastMessage)
        .lastMessageAt(conversation.getLastMessageAt())
        .createdAt(conversation.getCreatedAt())
        .build();
  }
}

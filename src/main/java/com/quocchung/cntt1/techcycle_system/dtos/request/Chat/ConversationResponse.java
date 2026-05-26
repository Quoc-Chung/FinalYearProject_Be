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
import java.util.Set;

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

  /**
   * For admin: filter out admins from participants list and use the first non-admin as creator
   */
  public static ConversationResponse fromEntityForAdmin(
      Conversation conversation,
      List<ConversationParticipant> participants,
      ChatMessageResponse lastMessage,
      Set<Long> adminUserIds
  ) {
    // Filter out admin users from participants
    List<ConversationParticipant> nonAdminParticipants = participants.stream()
        .filter(p -> !adminUserIds.contains(p.getUser().getUserId()))
        .toList();

    // Get the first non-admin user as creator
    UserSummary creatorSummary = null;
    if (!nonAdminParticipants.isEmpty()) {
      User firstNonAdmin = nonAdminParticipants.get(0).getUser();
      creatorSummary = UserSummary.builder()
          .userId(firstNonAdmin.getUserId())
          .fullName(firstNonAdmin.getFullName())
          .avatarUrl(firstNonAdmin.getAvatarUrl())
          .isOnline(false)
          .build();
    }

    List<UserSummary> participantSummaries = nonAdminParticipants.stream()
        .map(p -> UserSummary.builder()
            .userId(p.getUser().getUserId())
            .fullName(p.getUser().getFullName())
            .avatarUrl(p.getUser().getAvatarUrl())
            .isOnline(false)
            .build())
        .toList();

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

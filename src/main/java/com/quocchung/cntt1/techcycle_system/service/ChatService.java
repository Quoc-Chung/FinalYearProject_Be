package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

  ConversationResponse createConversation(
      CreateConversationRequest request,
      User creator
  );

  Page<ConversationResponse> getConversations(Long userId, Pageable pageable);

  ConversationResponse getConversation(Long conversationId, Long userId);

  ChatMessageResponse sendMessage(
      Long conversationId,
      ChatMessageRequest request,
      User sender
  );

  Page<ChatMessageResponse> getMessages(
      Long conversationId,
      Long userId,
      Pageable pageable
  );

  ChatMessageResponse getMessage(Long messageId, Long userId);

  void markAsRead(Long conversationId, Long userId);

  long getUnreadCount(Long conversationId, Long userId);

  List<ConversationResponse.UserSummary> getConversationParticipants(Long conversationId);

  void addParticipant(Long conversationId, Long userId, User admin);

  void removeParticipant(Long conversationId, Long userId, User admin);

  void leaveConversation(Long conversationId, User user);
}

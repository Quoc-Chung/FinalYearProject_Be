package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatContactResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ChatService {

  ConversationResponse createConversation(CreateConversationRequest request, Long creatorId);

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

  ConversationResponse findConversationBetweenUsers(Long userId1, Long userId2);

  Page<ChatContactResponse> getChatContacts(Long userId, Pageable pageable);

  // Admin APIs
  Page<ConversationResponse> getAllConversationsForAdmin(Pageable pageable);

  Page<ConversationResponse> getConversationsByAdminId(Long adminId, Pageable pageable);

  Page<ConversationResponse> getConversationsByAdminId(Long adminId, Pageable pageable, Set<Long> adminUserIds);

  ConversationResponse createConversationWithAdmin(Long userId);

  long countUnreadConversationsForAdmin();

  // User-to-Admin API
  ConversationResponse getOrCreateAdminConversation(Long userId);
}

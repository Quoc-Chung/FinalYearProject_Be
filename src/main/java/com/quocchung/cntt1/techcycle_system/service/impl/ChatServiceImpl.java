package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.AttachmentResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.*;
import com.quocchung.cntt1.techcycle_system.repository.*;
import com.quocchung.cntt1.techcycle_system.security.CustomUserDetailsService;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import com.quocchung.cntt1.techcycle_system.utils.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository participantRepository;
  private final MessageRepository messageRepository;
  private final MessageAttachmentRepository attachmentRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CustomUserDetailsService userDetailsService;

  @Override
  @Transactional
  public ConversationResponse createConversation(CreateConversationRequest request, User creator) {
    Conversation conversation = Conversation.builder()
        .createdBy(creator)
        .build();

    if (request.getPostId() != null) {
      Post post = postRepository.findById(request.getPostId())
          .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));
      conversation.setPost(post);
    }

    conversation = conversationRepository.save(conversation);

    ConversationParticipant creatorParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(creator)
        .isBlocked(false)
        .build();
    participantRepository.save(creatorParticipant);

    if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
      for (Long participantId : request.getParticipantIds()) {
        User participant = userRepository.findById(participantId)
            .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

        ConversationParticipant cp = ConversationParticipant.builder()
            .conversation(conversation)
            .user(participant)
            .isBlocked(false)
            .build();
        participantRepository.save(cp);
      }
    }

    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversation.getConversationId());

    if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
      ChatMessageRequest messageRequest = ChatMessageRequest.builder()
          .messageType(MessageType.TEXT)
          .content(request.getInitialMessage())
          .build();
      sendMessage(conversation.getConversationId(), messageRequest, creator);
    }

    return ConversationResponse.fromEntity(conversation, participants, null);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ConversationResponse> getConversations(Long userId, Pageable pageable) {
    Page<Conversation> conversations = conversationRepository
        .findByParticipantUserIdOrderByLastMessageAtDesc(userId, pageable);

    return conversations.map(conv -> {
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conv.getConversationId());

      ChatMessageResponse lastMessage = messageRepository
          .findLastMessageByConversationId(conv.getConversationId())
          .map(ChatMessageResponse::fromEntity)
          .orElse(null);

      return ConversationResponse.fromEntity(conv, participants, lastMessage);
    });
  }

  @Override
  @Transactional(readOnly = true)
  public ConversationResponse getConversation(Long conversationId, Long userId) {
    Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
        .orElseThrow(() -> new ResException(ResErrorCode.CONVERSATION_NOT_FOUND));

    if (!participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId)) {
      throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
    }

    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversationId);

    ChatMessageResponse lastMessage = messageRepository
        .findLastMessageByConversationId(conversationId)
        .map(ChatMessageResponse::fromEntity)
        .orElse(null);

    return ConversationResponse.fromEntity(conversation, participants, lastMessage);
  }

  @Override
  @Transactional
  public ChatMessageResponse sendMessage(Long conversationId, ChatMessageRequest request, User sender) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ResException(ResErrorCode.CONVERSATION_NOT_FOUND));

    if (!participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, sender.getUserId())) {
      throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
    }

    Message message = Message.builder()
        .conversation(conversation)
        .sender(sender)
        .messageType(request.getMessageType())
        .content(request.getContent())
        .isRead(false)
        .build();

    message = messageRepository.save(message);

    List<AttachmentResponse> attachments = new ArrayList<>();
    if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
      for (ChatMessageRequest.AttachmentInfo info : request.getAttachments()) {
        MessageAttachment attachment = MessageAttachment.builder()
            .message(message)
            .objectKey(info.getObjectKey())
            .mimeType(info.getMimeType())
            .fileSize(info.getFileSize())
            .durationSeconds(info.getDurationSeconds())
            .build();
        attachmentRepository.save(attachment);

        attachments.add(AttachmentResponse.builder()
            .objectKey(info.getObjectKey())
            .mimeType(info.getMimeType())
            .fileSize(info.getFileSize())
            .url(info.getObjectKey())
            .build());
      }
    }

    conversation.setLastMessageAt(LocalDateTime.now());
    conversationRepository.save(conversation);

    return ChatMessageResponse.fromEntityWithAttachments(message, attachments);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatMessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable) {
    if (!participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId)) {
      throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
    }

    Page<Message> messages = messageRepository
        .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

    return messages.map(ChatMessageResponse::fromEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public ChatMessageResponse getMessage(Long messageId, Long userId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new ResException(ResErrorCode.MESSAGE_NOT_FOUND));

    if (!participantRepository.existsByConversationConversationIdAndUserUserId(
        message.getConversation().getConversationId(), userId)) {
      throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
    }

    List<AttachmentResponse> attachments = attachmentRepository
        .findByMessageMessageId(messageId)
        .stream()
        .map(AttachmentResponse::fromEntity)
        .toList();

    return ChatMessageResponse.fromEntityWithAttachments(message, attachments);
  }

  @Override
  @Transactional
  public void markAsRead(Long conversationId, Long userId) {
    if (!participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId)) {
      throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
    }

    messageRepository.markMessagesAsRead(conversationId, userId);
    participantRepository.updateLastReadAt(conversationId, userId, LocalDateTime.now());
  }

  @Override
  @Transactional(readOnly = true)
  public long getUnreadCount(Long conversationId, Long userId) {
    return messageRepository.countUnreadMessages(conversationId, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationResponse.UserSummary> getConversationParticipants(Long conversationId) {
    return participantRepository.findByConversationIdWithUser(conversationId).stream()
        .map(p -> ConversationResponse.UserSummary.builder()
            .userId(p.getUser().getUserId())
            .fullName(p.getUser().getFullName())
            .avatarUrl(p.getUser().getAvatarUrl())
            .isOnline(false)
            .build())
        .toList();
  }

  @Override
  @Transactional
  public void addParticipant(Long conversationId, Long userId, User admin) {
    if (!hasAdminRights(conversationId, admin)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    User newParticipant = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ResException(ResErrorCode.CONVERSATION_NOT_FOUND));

    if (participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId)) {
      throw new ResException(ResErrorCode.USER_ALREADY_PARTICIPANT);
    }

    ConversationParticipant participant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(newParticipant)
        .isBlocked(false)
        .build();

    participantRepository.save(participant);
  }

  @Override
  @Transactional
  public void removeParticipant(Long conversationId, Long userId, User admin) {
    if (!hasAdminRights(conversationId, admin)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    ConversationParticipant participant = participantRepository
        .findByConversationIdAndUserId(conversationId, userId)
        .orElseThrow(() -> new ResException(ResErrorCode.PARTICIPANT_NOT_FOUND));

    participantRepository.delete(participant);
  }

  @Override
  @Transactional
  public void leaveConversation(Long conversationId, User user) {
    ConversationParticipant participant = participantRepository
        .findByConversationIdAndUserId(conversationId, user.getUserId())
        .orElseThrow(() -> new ResException(ResErrorCode.PARTICIPANT_NOT_FOUND));

    participantRepository.delete(participant);
  }

  private boolean hasAdminRights(Long conversationId, User user) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ResException(ResErrorCode.CONVERSATION_NOT_FOUND));

    if (conversation.getCreatedBy().getUserId().equals(user.getUserId())) {
      return true;
    }

    Set<GrantedAuthority> authorities = userDetailsService.buildAuthorities(user.getUserId());
    return authorities.stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") 
                       || auth.getAuthority().equals("ROLE_ADMINISTRATOR"));
  }
}

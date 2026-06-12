package com.quocchung.cntt1.techcycle_system.service.impl;
import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.config.WebSocketEventListener;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.AttachmentResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatContactResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.*;
import com.quocchung.cntt1.techcycle_system.repository.*;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private final WebSocketEventListener eventListener;
  private final MinioProperties minioProperties;
  private final NotificationService notificationService;

  private String buildPublicUrl(String objectKey) {
    String base = minioProperties.getPublicEndpoint();
    if (base == null || base.isBlank()) {
      base = minioProperties.getEndpoint();
    }
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/" + minioProperties.getBucketName() + "/" + objectKey;
  }

  @Override
  @Transactional
  public ConversationResponse createConversation(CreateConversationRequest request, Long creatorId) {

    User creator = userRepository.findById(creatorId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    User participant = userRepository.findById(request.getParticipantId())
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Optional<Conversation> existingConversation = conversationRepository
        .findDirectConversation(creatorId, participant.getUserId(), 2);

    if (existingConversation.isPresent()) {
      Conversation conversation = existingConversation.get();
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conversation.getConversationId());
      return ConversationResponse.fromEntity(conversation, participants, null);
    }

    Conversation conversation = Conversation.builder()
        .createdBy(creator)
        .build();

    conversation = conversationRepository.save(conversation);

    ConversationParticipant creatorParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(creator)
        .isBlocked(false)
        .build();

    participantRepository.save(creatorParticipant);

    ConversationParticipant participantObj = ConversationParticipant.builder()
        .conversation(conversation)
        .user(participant)
        .isBlocked(false)
        .build();

    participantRepository.save(participantObj);

    List<ConversationParticipant> participants =
        participantRepository.findByConversationIdWithUser(
            conversation.getConversationId()
        );
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
        MediaType mediaType = MediaType.FILE;

        MessageAttachment attachment = MessageAttachment.builder()
            .message(message)
            .objectKey(info.getObjectKey())
            .mimeType(info.getMimeType())
            .fileSize(info.getFileSize())
            .durationSeconds(info.getDurationSeconds())
            .mediaType(mediaType)
            .build();
        attachmentRepository.save(attachment);

        attachments.add(AttachmentResponse.builder()
            .objectKey(info.getObjectKey())
            .mimeType(info.getMimeType())
            .fileSize(info.getFileSize())
            .url(buildPublicUrl(info.getObjectKey()))
            .mediaType(mediaType)
            .durationSeconds(info.getDurationSeconds())
            .build());
      }
    }

    conversation.setLastMessageAt(LocalDateTime.now());
    conversationRepository.save(conversation);

    sendChatNotification(conversation, sender, message);

    boolean isAdminSender = userRepository.hasRole(sender.getUserId(), "ADMIN");
    if (!isAdminSender) {
      // Lấy tất cả participants của cuộc hội thoại
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conversationId);

      boolean hasAdminParticipant = participants.stream()
          .filter(p -> !p.getUser().getUserId().equals(sender.getUserId()))
          .anyMatch(p -> userRepository.hasRole(p.getUser().getUserId(), "ADMIN"));

      if (hasAdminParticipant) {
        List<User> admins = userRepository.getAllAdmin();
        for (User admin : admins) {
          String title = "Tin nhắn mới từ người dùng";
          String content = sender.getFullName() + ": " +
              (message.getContent() != null && !message.getContent().isBlank()
                  ? (message.getContent().length() > 50
                      ? message.getContent().substring(0, 50) + "..."
                      : message.getContent())
                  : "Đã gửi tệp đính kèm");
          String targetUrl = "/admin/messages/" + conversationId;
          java.util.Map<String, Object> notiData = new java.util.HashMap<>();
          notiData.put("conversationId", conversationId);
          notiData.put("senderId", sender.getUserId());
          notiData.put("senderName", sender.getFullName());

          notificationService.createNotification(
              admin,
              sender,
              NotificationType.CHAT_MESSAGE,
              title,
              content,
              targetUrl,
              notiData
          );
        }
      }
    }

    return ChatMessageResponse.fromEntityWithAttachments(message, attachments);
  }

  private void sendChatNotification(Conversation conversation, User sender, Message message) {
    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversation.getConversationId());

    Map<String, Object> data = Map.of(
        "conversationId", conversation.getConversationId(),
        "messageId", message.getMessageId()
    );

    for (ConversationParticipant participant : participants) {
      if (participant.getUser().getUserId().equals(sender.getUserId())) {
        continue;
      }

      String preview = message.getContent() != null
          ? (message.getContent().length() > 50
              ? message.getContent().substring(0, 50) + "..."
              : message.getContent())
          : "Đã gửi tệp đính kèm";

      notificationService.createNotification(
          participant.getUser(),
          sender,
          NotificationType.CHAT_MESSAGE,
          sender.getFullName() + " đã nhắn tin cho bạn",
          preview,
          "/messages?conversation=" + conversation.getConversationId(),
          data
      );
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatMessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable) {
    if (userId != null) {
      if (!participantRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId)) {
        throw new ResException(ResErrorCode.CONVERSATION_FORBIDDEN);
      }
    }
    Page<Message> messages = messageRepository
        .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

    List<Long> messageIds = messages.getContent().stream()
        .map(Message::getMessageId)
        .toList();
    Map<Long, List<AttachmentResponse>> attachmentMap = attachmentRepository
        .findByMessageIds(messageIds)
        .stream()
        .collect(Collectors.groupingBy(
            att -> att.getMessage().getMessageId(),
            Collectors.mapping(att -> AttachmentResponse.builder()
                    .attachmentId(att.getAttachmentId())
                    .mediaType(att.getMediaType())
                    .objectKey(att.getObjectKey())
                    .url(buildPublicUrl(att.getObjectKey()))
                    .mimeType(att.getMimeType())
                    .fileSize(att.getFileSize())
                    .durationSeconds(att.getDurationSeconds())
                    .build(),
                Collectors.toList()
            )
        ));
    return messages.map(message -> {
      List<AttachmentResponse> attachments = attachmentMap
          .getOrDefault(message.getMessageId(), null);
      return ChatMessageResponse.fromEntityWithAttachments(message, attachments);
    });
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
  public ConversationResponse findConversationBetweenUsers(Long userId1, Long userId2) {
    Optional<Conversation> conversation = conversationRepository
        .findDirectConversation(userId1, userId2, 2);

    if (conversation.isEmpty()) {
      return null;
    }

    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversation.get().getConversationId());

    ChatMessageResponse lastMessage = messageRepository
        .findLastMessageByConversationId(conversation.get().getConversationId())
        .map(ChatMessageResponse::fromEntity)
        .orElse(null);

    return ConversationResponse.fromEntity(conversation.get(), participants, lastMessage);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatContactResponse> getChatContacts(Long userId, Pageable pageable) {
    Page<Object[]> results = conversationRepository.findChatContactsByUserId(userId, pageable);

    return results.map(row -> {
      User user = (User) row[0];
      java.time.LocalDateTime lastMessageAt = (java.time.LocalDateTime) row[1];
      boolean isOnline = eventListener.isUserOnline(user.getUserId());
      return ChatContactResponse.fromUser(user, isOnline, lastMessageAt);
    });
  }

  // ========== ADMIN APIS ==========

  @Override
  @Transactional(readOnly = true)
  public Page<ConversationResponse> getAllConversationsForAdmin(Pageable pageable) {
    Page<Conversation> conversations = conversationRepository.findAllConversationsForAdmin(pageable);

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
  public Page<ConversationResponse> getConversationsByAdminId(Long adminId, Pageable pageable) {
    Page<Conversation> conversations = conversationRepository.findConversationsByAdminId(adminId, pageable);

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
  public Page<ConversationResponse> getConversationsByAdminId(Long adminId, Pageable pageable, Set<Long> adminUserIds) {
    Page<Conversation> conversations = conversationRepository.findConversationsByAdminId(adminId, pageable);

    return conversations.map(conv -> {
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conv.getConversationId());

      ChatMessageResponse lastMessage = messageRepository
          .findLastMessageByConversationId(conv.getConversationId())
          .map(ChatMessageResponse::fromEntity)
          .orElse(null);

      return ConversationResponse.fromEntityForAdmin(conv, participants, lastMessage, adminUserIds);
    });
  }








  @Override
  @Transactional
  public ConversationResponse createConversationWithAdmin(Long userId) {
    // Find any admin user
    List<User> admins = userRepository.findAllByRoleName("ADMIN");
    if (admins.isEmpty()) {
      throw new ResException(ResErrorCode.USER_NOT_FOUND);
    }
    User admin = admins.get(0);

    // Check if conversation already exists
    Optional<Conversation> existingConversation = conversationRepository
        .findDirectConversation(userId, admin.getUserId(), 2);

    if (existingConversation.isPresent()) {
      Conversation conversation = existingConversation.get();
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conversation.getConversationId());
      ChatMessageResponse lastMessage = messageRepository
          .findLastMessageByConversationId(conversation.getConversationId())
          .map(ChatMessageResponse::fromEntity)
          .orElse(null);
      return ConversationResponse.fromEntity(conversation, participants, lastMessage);
    }

    // Create new conversation
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Conversation conversation = Conversation.builder()
        .createdBy(user)
        .build();
    conversation = conversationRepository.save(conversation);

    // Add user as participant
    ConversationParticipant userParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(user)
        .isBlocked(false)
        .build();
    participantRepository.save(userParticipant);

    // Add admin as participant
    ConversationParticipant adminParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(admin)
        .isBlocked(false)
        .build();
    participantRepository.save(adminParticipant);

    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversation.getConversationId());
    return ConversationResponse.fromEntity(conversation, participants, null);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnreadConversationsForAdmin() {
    // Count all unread messages across all conversations
    return messageRepository.countUnreadMessagesForAdmin();
  }

  // ========== USER-TO-ADMIN IMPLEMENTATION ==========

  @Override
  @Transactional
  public ConversationResponse getOrCreateAdminConversation(Long userId) {
    // Find any admin user
    List<User> admins = userRepository.findAllByRoleName("ADMIN");
    if (admins.isEmpty()) {
      throw new ResException(ResErrorCode.USER_NOT_FOUND);
    }
    User admin = admins.get(0);

    // Check if conversation exists
    Optional<Conversation> existingConversation = conversationRepository
        .findDirectConversation(userId, admin.getUserId(), 2);

    if (existingConversation.isPresent()) {
      Conversation conversation = existingConversation.get();
      List<ConversationParticipant> participants = participantRepository
          .findByConversationIdWithUser(conversation.getConversationId());
      ChatMessageResponse lastMessage = messageRepository
          .findLastMessageByConversationId(conversation.getConversationId())
          .map(ChatMessageResponse::fromEntity)
          .orElse(null);
      return ConversationResponse.fromEntity(conversation, participants, lastMessage);
    }

    // Create new conversation
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Conversation conversation = Conversation.builder()
        .createdBy(user)
        .build();
    conversation = conversationRepository.save(conversation);

    // Add user as participant
    ConversationParticipant userParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(user)
        .isBlocked(false)
        .build();
    participantRepository.save(userParticipant);

    // Add admin as participant
    ConversationParticipant adminParticipant = ConversationParticipant.builder()
        .conversation(conversation)
        .user(admin)
        .isBlocked(false)
        .build();
    participantRepository.save(adminParticipant);

    List<ConversationParticipant> participants = participantRepository
        .findByConversationIdWithUser(conversation.getConversationId());
    return ConversationResponse.fromEntity(conversation, participants, null);
  }
}

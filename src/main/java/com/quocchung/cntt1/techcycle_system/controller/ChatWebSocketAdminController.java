package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.config.WebSocketEventListener;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.TypingNotification;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/message-admin")
public class ChatWebSocketAdminController {
  private final ChatService chatService;
  private final WebSocketEventListener eventListener;
  private final SimpMessagingTemplate messagingTemplate;
  private final ResponseUtils responseUtils;
  private final UserRepository userRepository;

  @PostMapping("/admin/user/{userId}")
  public ResponseEntity<APIResponse<ConversationResponse>> createConversationWithUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ConversationResponse conversation = chatService.createConversationWithAdmin(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(conversation));
  }

  /**
   * Lấy tất cả cuộc trò chuyện mà admin này tham gia
   */
  @GetMapping("/admin/my-conversations")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<ConversationResponse>> getMyConversationsForAdmin(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
    Page<ConversationResponse> conversations = chatService.getConversationsByAdminId(userPrincipal.getUserId(), pageable);
    List<ConversationResponse> content = conversations.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, conversations.getTotalElements(), size));
  }

  /**
   * Lấy tin nhắn trong một cuộc trò chuyện giữa user và admin.
   */
  @GetMapping("/admin/messages/{conversationId}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessagesForAdmin(
      @PathVariable Long conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<ChatMessageResponse> messages = chatService.getMessages(conversationId, null, pageable);
    List<ChatMessageResponse> content = messages.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, messages.getTotalElements(), size));
  }

  /**
   * Gửi tin nhắn từ admin đến một cuộc trò chuyện.
   */
  @PostMapping("/admin/send/{conversationId}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<ChatMessageResponse>> sendMessageAsAdmin(
      @PathVariable Long conversationId,
      @RequestBody ChatMessageRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    User admin = userRepository.findById(userPrincipal.getUserId())
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));
    ChatMessageResponse message = chatService.sendMessage(conversationId, request, admin);
    return ResponseEntity.ok(responseUtils.success(message));
  }

  /**
   * Gửi tin nhắn trong một cuộc trò chuyện.
   */
  @PostMapping("/send/{conversationId}")
  public ResponseEntity<APIResponse<ChatMessageResponse>> sendMessage(
      @PathVariable Long conversationId,
      @RequestBody ChatMessageRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    User user = userRepository.findById(userPrincipal.getUserId())
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));
    ChatMessageResponse message = chatService.sendMessage(conversationId, request, user);
    return ResponseEntity.ok(responseUtils.success(message));
  }

  // ========== WEBSOCKET ENDPOINTS ==========

  /**
   * WebSocket: Gửi tin nhắn từ admin qua WebSocket
   * Subscribe: /topic/conversation.{conversationId}
   */
  @MessageMapping("/admin.chat.send/{conversationId}")
  public void sendMessageAsAdminWS(
      @DestinationVariable Long conversationId,
      @Payload ChatMessageRequest request,
      Principal principal
  ) {
    User admin = getUserFromPrincipal(principal);
    ChatMessageResponse response = chatService.sendMessage(conversationId, request, admin);
    log.info("[Admin WS] Sending message to conversation {}: {}", conversationId, response);

    // Send to specific conversation topic
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId,
        response
    );

    // Also broadcast to admin topic
    messagingTemplate.convertAndSend(
        "/topic/admin/messages",
        response
    );
  }

  /**
   * WebSocket: Admin typing notification
   */
  @MessageMapping("/admin.chat.typing/{conversationId}")
  public void handleTypingAdmin(
      @DestinationVariable Long conversationId,
      @Payload TypingNotification notification,
      Principal principal
  ) {
    User admin = getUserFromPrincipal(principal);

    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId + ".typing",
        new TypingEvent(conversationId, admin.getUserId(), admin.getFullName(), admin.getAvatarUrl(), notification.isTyping())
    );
  }

  private User getUserFromPrincipal(Principal principal) {
    if (!(principal instanceof UsernamePasswordAuthenticationToken authToken)) {
      throw new IllegalArgumentException("Unauthenticated websocket user");
    }

    Object principalObj = authToken.getPrincipal();

    if (!(principalObj instanceof UserPrincipal userPrincipal)) {
      throw new IllegalArgumentException("Invalid websocket principal");
    }

    return userRepository.findById(userPrincipal.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Exception e) {
    log.error("Admin WebSocket error: ", e);
    return "Error: " + e.getMessage();
  }

  // Inner class for typing event
  private record TypingEvent(Long conversationId, Long userId, String fullName, String avatarUrl, boolean isTyping) {}
}

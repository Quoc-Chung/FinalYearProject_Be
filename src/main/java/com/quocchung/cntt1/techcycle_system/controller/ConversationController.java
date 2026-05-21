package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.config.WebSocketEventListener;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ChatService chatService;
  private final UserRepository userRepository;
  private final WebSocketEventListener eventListener;
  private final ResponseUtils responseUtils;

  /**
   Tạo cuộc trò chuyện mới.
   Người dùng hiện tại sẽ là người tạo (creator).
   * @param request
   * @param userDetails
   * @return
   */
  @PostMapping
  public ResponseEntity<APIResponse<ConversationResponse>> createConversation(
      @Valid @RequestBody CreateConversationRequest request,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User creator = getUser(userDetails);
    ConversationResponse conversation = chatService.createConversation(request, creator);
    return ResponseEntity.ok(responseUtils.success(conversation));
  }

  @GetMapping("/conversations")
  public ResponseEntity<APIResponse<ConversationResponse>> getConversations(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    User user = getUser(userDetails);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
    Page<ConversationResponse> conversations = chatService.getConversations(user.getUserId(), pageable);
    List<ConversationResponse> content = conversations.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, conversations.getTotalElements(), size));
  }

  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<APIResponse<ConversationResponse>> getConversation(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    ConversationResponse conversation = chatService.getConversation(conversationId, user.getUserId());
    return ResponseEntity.ok(responseUtils.success(conversation));
  }

  @GetMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessages(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size
  ) {
    User user = getUser(userDetails);
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatMessageResponse> messages = chatService.getMessages(conversationId, user.getUserId(), pageable);
    List<ChatMessageResponse> content = messages.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, messages.getTotalElements(), size));
  }

  @GetMapping("/conversations/{conversationId}/messages/{messageId}")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessage(
      @PathVariable Long conversationId,
      @PathVariable Long messageId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    ChatMessageResponse message = chatService.getMessage(messageId, user.getUserId());
    return ResponseEntity.ok(responseUtils.success(message));
  }

  @GetMapping("/conversations/{conversationId}/participants")
  public ResponseEntity<APIResponse<ConversationResponse.UserSummary>> getParticipants(
      @PathVariable Long conversationId
  ) {
    List<ConversationResponse.UserSummary> participants = chatService.getConversationParticipants(conversationId);
    return ResponseEntity.ok(responseUtils.successList(participants));
  }

  @GetMapping("/conversations/{conversationId}/unread")
  public ResponseEntity<APIResponse<Long>> getUnreadCount(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    long count = chatService.getUnreadCount(conversationId, user.getUserId());
    return ResponseEntity.ok(responseUtils.success(count));
  }

  @PostMapping("/conversations/{conversationId}/read")
  public ResponseEntity<APIResponse<Void>> markAsRead(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    chatService.markAsRead(conversationId, user.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PostMapping("/conversations/{conversationId}/participants/{userId}")
  public ResponseEntity<APIResponse<Void>> addParticipant(
      @PathVariable Long conversationId,
      @PathVariable Long userId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User admin = getUser(userDetails);
    chatService.addParticipant(conversationId, userId, admin);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @DeleteMapping("/conversations/{conversationId}/participants/{userId}")
  public ResponseEntity<APIResponse<Void>> removeParticipant(
      @PathVariable Long conversationId,
      @PathVariable Long userId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User admin = getUser(userDetails);
    chatService.removeParticipant(conversationId, userId, admin);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @DeleteMapping("/conversations/{conversationId}/leave")
  public ResponseEntity<APIResponse<Void>> leaveConversation(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    chatService.leaveConversation(conversationId, user);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @GetMapping("/users/{userId}/online")
  public ResponseEntity<APIResponse<Boolean>> isUserOnline(@PathVariable Long userId) {
    boolean online = eventListener.isUserOnline(userId);
    return ResponseEntity.ok(responseUtils.success(online));
  }

  @GetMapping("/users/online")
  public ResponseEntity<APIResponse<Long>> getOnlineUsers() {
    List<Long> onlineUsers = List.copyOf(eventListener.getOnlineUsers());
    return ResponseEntity.ok(responseUtils.successList(onlineUsers));
  }

  private User getUser(UserDetails userDetails) {
    return userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}

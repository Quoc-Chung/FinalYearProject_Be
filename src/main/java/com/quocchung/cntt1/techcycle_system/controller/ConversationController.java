package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.config.WebSocketEventListener;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatContactResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ChatService chatService;
  private final WebSocketEventListener eventListener;
  private final ResponseUtils responseUtils;

  /**
   * Tạo một cuộc trò chuyện mới.
   */
  @PostMapping
  public ResponseEntity<APIResponse<ConversationResponse>> createConversation(
      @Valid @RequestBody CreateConversationRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ConversationResponse conversation = chatService.createConversation(request, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(conversation));
  }
  /**
   * Lấy danh sách cuộc trò chuyện của người dùng hiện tại với phân trang.
   */
  @GetMapping("/list")
  public ResponseEntity<APIResponse<ConversationResponse>> getConversations(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
    Page<ConversationResponse> conversations = chatService.getConversations(userPrincipal.getUserId(), pageable);
    List<ConversationResponse> content = conversations.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, conversations.getTotalElements(), size));
  }
  /**
   * Lấy danh sách liên hệ đã nhắn tin.
   */
  @GetMapping("/contacts")
  public ResponseEntity<APIResponse<ChatContactResponse>> getChatContacts(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "200") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ChatContactResponse> contacts = chatService.getChatContacts(userPrincipal.getUserId(), pageable);
    List<ChatContactResponse> content = contacts.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, contacts.getTotalElements(), size));
  }

  /**
   * Lấy thông tin chi tiết của một cuộc trò chuyện cụ thể.
   */
  @GetMapping("/{conversationId}")
  public ResponseEntity<APIResponse<ConversationResponse>> getConversation(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ConversationResponse conversation = chatService.getConversation(conversationId, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(conversation));
  }

  /**
   * Kiểm tra xem đã tồn tại cuộc trò chuyện giữa người dùng hiện tại và userId được truyền lên chưa.
   * Nếu đã tồn tại, trả về thông tin cuộc trò chuyện.
   * Nếu chưa tồn tại, trả về null.
   *
   * @param targetUserId   ID của người dùng cần kiểm tra
   * @param userPrincipal  Thông tin người dùng hiện tại
   * @return Thông tin cuộc trò chuyện nếu tồn tại, null nếu chưa có
   */
  @GetMapping("/check/{targetUserId}")
  public ResponseEntity<APIResponse<ConversationResponse>> checkConversationExists(
      @PathVariable Long targetUserId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ConversationResponse conversation = chatService.findConversationBetweenUsers(
        userPrincipal.getUserId(),
        targetUserId
    );
    return ResponseEntity.ok(responseUtils.success(conversation));
  }
  /**
   * Lấy danh sách tin nhắn trong một cuộc trò chuyện với phân trang.
   */
  @GetMapping("/messages/{conversationId}")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessages(
      @PathVariable Long conversationId,
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatMessageResponse> messages = chatService.getMessages(conversationId, userPrincipal.getUserId(), pageable);
    List<ChatMessageResponse> content = messages.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, messages.getTotalElements(), size));
  }

  /**
   * Lấy thông tin chi tiết của một tin nhắn cụ thể.
   */
  @GetMapping("/messages/detail/{messageId}")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessage(
      @PathVariable Long messageId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ChatMessageResponse message = chatService.getMessage(messageId, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(message));
  }

  /**
   * Kiểm tra xem một người dùng có đang online hay không.
   */
  @GetMapping("/users/online/{userId}")
  public ResponseEntity<APIResponse<Boolean>> isUserOnline(@PathVariable Long userId) {
    boolean online = eventListener.isUserOnline(userId);
    return ResponseEntity.ok(responseUtils.success(online));
  }

}

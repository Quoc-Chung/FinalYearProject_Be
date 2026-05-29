package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.config.WebSocketEventListener;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ConversationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.CreateConversationRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatContactResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ChatService chatService;
  private final WebSocketEventListener eventListener;
  private final ResponseUtils responseUtils;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

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
   * Tạo hoặc lấy cuộc trò chuyện với admin.
   * POST /api/conversations/with-admin
   */
  @PostMapping("/with-admin")
  public ResponseEntity<APIResponse<ConversationResponse>> createOrGetAdminConversation(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ConversationResponse conversation = chatService.createConversationWithAdmin(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(conversation));
  }
  /**
   * Lấy danh sách cuộc trò chuyện của người dùng hiện tại với phân trang.
   * Tự động phân biệt: USER → conversations của user, ADMIN → conversations của admin (đã lọc admin)
   */
  @GetMapping("/list")
  public ResponseEntity<APIResponse<ConversationResponse>> getConversations(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));


    boolean isAdmin = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ADMIN"));

    Page<ConversationResponse> conversations;
    // neu la admin
    if (isAdmin) {
      List<User> admins = userRepository.findAllByRoleName("ADMIN");
      Set<Long> adminUserIds = admins.stream()
          .map(User::getUserId)
          .collect(Collectors.toSet());

      conversations = chatService.getConversationsByAdminId(userPrincipal.getUserId(), pageable, adminUserIds);
    } else {
      Page<ConversationResponse> allConversations = chatService.getConversations(userPrincipal.getUserId(), pageable);
      List<User> admins = userRepository.findAllByRoleName("ADMIN");
      Set<Long> adminUserIds = admins.stream()
          .map(User::getUserId)
          .collect(Collectors.toSet());

      List<ConversationResponse> filteredContent = allConversations.getContent().stream()
          .filter(conv -> {
            boolean allParticipantsAreAdmins = conv.getParticipants().stream()
                .filter(p -> !p.getUserId().equals(userPrincipal.getUserId()))
                .allMatch(p -> adminUserIds.contains(p.getUserId()));
            return !allParticipantsAreAdmins;
          })
          .collect(Collectors.toList());

      conversations = new org.springframework.data.domain.PageImpl<>(
          filteredContent,
          pageable,
          allConversations.getTotalElements()
      );
    }

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
      @RequestParam(defaultValue = "500") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatMessageResponse> messages = chatService.getMessages(conversationId, userPrincipal.getUserId(), pageable);
    List<ChatMessageResponse> content = messages.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, messages.getTotalElements(), size));
  }

  /**
   * Lấy tin nhắn cho admin (không cần kiểm tra quyền user trong conversation).
   * GET /api/conversations/admin/messages/{conversationId}
   */
  @GetMapping("/admin/messages/{conversationId}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<ChatMessageResponse>> getMessagesForAdmin(
      @PathVariable Long conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "500") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<ChatMessageResponse> messages = chatService.getMessages(conversationId, null, pageable);
    List<ChatMessageResponse> content = messages.getContent();
    return ResponseEntity.ok(responseUtils.successPage(content, page, messages.getTotalElements(), size));
  }

  /**
   * Gửi tin nhắn từ người dùng thường.
   * POST /api/conversations/send/{conversationId}
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

    // Broadcast to WebSocket for real-time updates
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId,
        message
    );
    messagingTemplate.convertAndSend(
        "/topic/admin/messages",
        message
    );

    return ResponseEntity.ok(responseUtils.success(message));
  }

  /**
   * Gửi tin nhắn từ admin.
   * POST /api/conversations/admin/send/{conversationId}
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

    // Broadcast to WebSocket for real-time updates
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId,
        message
    );
    messagingTemplate.convertAndSend(
        "/topic/admin/messages",
        message
    );

    return ResponseEntity.ok(responseUtils.success(message));
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

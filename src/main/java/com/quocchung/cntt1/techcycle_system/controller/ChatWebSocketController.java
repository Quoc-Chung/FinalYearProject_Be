package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ReadReceiptEvent;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.TypingEvent;
import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.TypingNotification;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controller xử lý các tin nhắn WebSocket cho chức năng chat real-time.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;
  private final UserRepository userRepository;

  @MessageMapping("/chat.send/{conversationId}")
  public void sendMessage(
      @DestinationVariable Long conversationId,
      @Payload ChatMessageRequest request,
      Principal principal
  ) {
    User sender = getUserFromPrincipal(principal);
    ChatMessageResponse response = chatService.sendMessage(conversationId, request, sender);
    log.info(
        "[WS] Sending message to conversation {}: {}",
        conversationId,
        response
    );
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId,
        response
    );
  }

  @MessageMapping("/chat.typing/{conversationId}")
  public void handleTyping(
      @DestinationVariable Long conversationId,
      @Payload TypingNotification notification,
      Principal principal
  ) {
    User sender = getUserFromPrincipal(principal);

    TypingEvent event = new TypingEvent(
        conversationId,
        sender.getUserId(),
        sender.getFullName(),
        sender.getAvatarUrl(),
        notification.isTyping()
    );
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId + ".typing",
        event
    );
  }

  @MessageMapping("/chat.read/{conversationId}")
  public void markAsRead(
      @DestinationVariable Long conversationId,
      Principal principal
  ) {
    User user = getUserFromPrincipal(principal);
    chatService.markAsRead(conversationId, user.getUserId());

    ReadReceiptEvent event = new ReadReceiptEvent(
        conversationId,
        user.getUserId(),
        user.getFullName() != null ? user.getFullName() : user.getEmail(),
        user.getAvatarUrl(),
        System.currentTimeMillis()
    );
    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId + ".read",
        event
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
    log.error("WebSocket error: ", e);
    return "Error: " + e.getMessage();
  }
}

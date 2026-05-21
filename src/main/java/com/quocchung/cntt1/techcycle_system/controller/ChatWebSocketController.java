package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Chat.ChatMessageRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Chat.ChatMessageResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

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
    log.info("User {} sending message to conversation {}", sender.getUserId(), conversationId);

    ChatMessageResponse response = chatService.sendMessage(conversationId, request, sender);

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
    log.debug("User {} is typing in conversation {}", sender.getUserId(), conversationId);

    TypingEvent event = new TypingEvent(
        conversationId,
        sender.getUserId(),
        sender.getFullName(),
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
    log.debug("User {} marking messages as read in conversation {}", user.getUserId(), conversationId);

    chatService.markAsRead(conversationId, user.getUserId());

    ReadReceiptEvent event = new ReadReceiptEvent(
        conversationId,
        user.getUserId(),
        System.currentTimeMillis()
    );

    messagingTemplate.convertAndSend(
        "/topic/conversation." + conversationId + ".read",
        event
    );
  }

  private User getUserFromPrincipal(Principal principal) {
    String email = principal.getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Exception e) {
    log.error("WebSocket error: ", e);
    return "Error: " + e.getMessage();
  }

  public record TypingNotification(boolean isTyping) {}
  public record TypingEvent(Long conversationId, Long userId, String userName, boolean isTyping) {}
  public record ReadReceiptEvent(Long conversationId, Long userId, long timestamp) {}
}

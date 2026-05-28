package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller để client subscribe nhận thông báo real-time.
 * Client subscribe: /user/queue/notifications
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/notification.subscribe")
  public void subscribeNotifications(
      @Payload String userId,
      UserPrincipal principal
  ) {
    log.info("[WS-NOTI] User {} subscribed to notifications", principal.getUserId());
  }
}

package com.quocchung.cntt1.techcycle_system.config;

import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final Map<String, Long> sessionUserMap =
      new ConcurrentHashMap<>();

  private final Set<Long> onlineUsers =
      ConcurrentHashMap.newKeySet();

  @EventListener
  public void handleWebSocketConnectListener(
      SessionConnectedEvent event
  ) {

    StompHeaderAccessor accessor =
        StompHeaderAccessor.wrap(event.getMessage());

    Principal principal = accessor.getUser();

    Long userId = extractUserId(principal);

    if (userId == null) {
      log.warn("[WS] Could not extract userId");
      return;
    }

    String sessionId = accessor.getSessionId();

    if (sessionId == null) {
      log.warn("[WS] SessionId is null");
      return;
    }

    sessionUserMap.put(sessionId, userId);

    onlineUsers.add(userId);

    log.info(
        "[WS] User connected: userId={}, sessionId={}",
        userId,
        sessionId
    );
  }

  @EventListener
  public void handleWebSocketDisconnectListener(
      SessionDisconnectEvent event
  ) {

    String sessionId = event.getSessionId();

    Long userId = sessionUserMap.remove(sessionId);

    if (userId == null) {
      return;
    }

    boolean hasOtherSessions =
        sessionUserMap.containsValue(userId);

    if (!hasOtherSessions) {
      onlineUsers.remove(userId);
    }

    log.info(
        "[WS] User disconnected: userId={}, sessionId={}",
        userId,
        sessionId
    );
  }

  private Long extractUserId(Principal principal) {

    if (!(principal instanceof UsernamePasswordAuthenticationToken authToken)) {
      return null;
    }

    Object principalObj = authToken.getPrincipal();

    if (principalObj instanceof UserPrincipal userPrincipal) {
      return userPrincipal.getUserId();
    }

    return null;
  }

  public boolean isUserOnline(Long userId) {
    return onlineUsers.contains(userId);
  }

  public Set<Long> getOnlineUsers() {
    return Set.copyOf(onlineUsers);
  }
}
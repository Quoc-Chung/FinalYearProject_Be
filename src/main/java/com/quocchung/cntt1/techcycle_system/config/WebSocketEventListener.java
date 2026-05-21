package com.quocchung.cntt1.techcycle_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
  private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal user = accessor.getUser();

    if (user != null) {
      String sessionId = accessor.getSessionId();
      sessionUserMap.put(sessionId, extractUserId(user));

      if (user instanceof UsernamePasswordAuthenticationToken authToken) {
        Object principal = authToken.getPrincipal();
        if (principal instanceof String email) {
          onlineUsers.add(extractUserId(user));
        }
      }
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    String sessionId = event.getSessionId();
    Long userId = sessionUserMap.remove(sessionId);

    if (userId != null) {
      boolean hasOtherSessions = sessionUserMap.containsValue(userId);
      if (!hasOtherSessions) {
        onlineUsers.remove(userId);
      }
    }
  }
  
  private Long extractUserId(Principal principal) {
    if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
      Object credentials = authToken.getCredentials();
      if (credentials instanceof Long userId) {
        return userId;
      }
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

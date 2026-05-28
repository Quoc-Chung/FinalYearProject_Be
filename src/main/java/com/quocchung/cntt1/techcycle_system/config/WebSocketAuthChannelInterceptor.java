package com.quocchung.cntt1.techcycle_system.config;

import com.quocchung.cntt1.techcycle_system.security.JwtService;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    // Chỉ auth khi CONNECT
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {

      String authHeader = accessor.getFirstNativeHeader("Authorization");

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        log.warn("[WS] Missing Authorization header");
        throw new IllegalArgumentException("Missing Authorization header");
      }

      try {

        String token = authHeader.substring(7);

        // Parse + verify JWT
        Claims claims = jwtService.parseClaims(token);

        String type = claims.get("type", String.class);

        if (!"access".equals(type)) {
          throw new IllegalArgumentException("Invalid token type");
        }

        Long userId = claims.get("uid", Long.class);
        String email = claims.getSubject();

        Object rawAuthorities = claims.get("authorities");

        List<SimpleGrantedAuthority> authorities =
            rawAuthorities instanceof List<?> list
                ? list.stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .toList()
                : List.of();

        UserPrincipal userPrincipal =
            new UserPrincipal(userId, email);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                authorities
            );
        accessor.setUser(authentication);

        log.info(
            "[WS] Authenticated successfully: userId={}, email={}",
            userId,
            email
        );

      } catch (Exception e) {

        log.error("[WS] Authentication failed: {}", e.getMessage());

        throw new IllegalArgumentException(
            "WebSocket authentication failed"
        );
      }
    }

    return message;
  }
}
package com.quocchung.cntt1.techcycle_system.config;

import com.quocchung.cntt1.techcycle_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");
      if (token != null && token.startsWith("Bearer ")) {
        try {
          String jwt = token.substring(7);
          if (jwtService.isAccessToken(jwt)) {
            Long userId = jwtService.getUserId(jwt);
            String email = jwtService.getEmail(jwt);
            List<String> authorities = jwtService.getAuthorities(jwt);

            List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, grantedAuthorities);

            accessor.setUser(authToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid token");
        }
      }
    }
    return message;
  }
}

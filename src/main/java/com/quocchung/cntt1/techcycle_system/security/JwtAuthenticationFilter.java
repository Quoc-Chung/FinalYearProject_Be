package com.quocchung.cntt1.techcycle_system.security;

import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final RedisTokenService redisTokenService;
  private final SecurityErrorResponseWriter responseWriter;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    try {
      if (redisTokenService.isAccessTokenBlacklisted(token) || !jwtService.isAccessToken(token)) {
        responseWriter.write(request, response, ResErrorCode.UNAUTHORIZED);
        return;
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        String email = jwtService.getEmail(token);
        List<SimpleGrantedAuthority> authorities = jwtService.getAuthorities(token).stream()
            .map(SimpleGrantedAuthority::new)
            .toList();

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(email, null, authorities);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception ex) {
      responseWriter.write(request, response, ResErrorCode.UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}

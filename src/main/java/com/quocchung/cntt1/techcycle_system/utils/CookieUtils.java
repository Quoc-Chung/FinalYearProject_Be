package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtils {

  private final AuthProperties authProperties;

  public String getRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie cookie : request.getCookies()) {
      if (authProperties.getRefreshCookieName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}

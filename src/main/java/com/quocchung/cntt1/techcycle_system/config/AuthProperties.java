package com.quocchung.cntt1.techcycle_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
  private String refreshCookieName = "refreshToken";
  private boolean refreshCookieSecure = true;
  private String refreshCookieSameSite = "Strict";
  private String refreshCookiePath = "/";
}

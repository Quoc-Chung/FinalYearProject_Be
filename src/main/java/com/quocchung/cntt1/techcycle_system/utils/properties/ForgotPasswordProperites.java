package com.quocchung.cntt1.techcycle_system.utils.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
@Configuration
public class ForgotPasswordProperites {

  @Value("${forgot-password.reset-password-token-ttl-seconds:900}")
  private long resetTokenTtlSeconds;

  @Value("${forgot-password.forgot-password-rate-limit.window-seconds:900}") // 15 phút
  private long forgotPasswordRateLimitWindowSeconds;

  @Value("${forgot-password.forgot-password-rate-limit.email-max-attempts:3}")
  private int forgotPasswordEmailMaxAttempts;

  @Value("${forgot-password.forgot-password-rate-limit.ip-max-attempts:10}")
  private int forgotPasswordIpMaxAttempts;

  @Value("${forgot-password.frontend-reset-password-url:http://localhost:3000/reset-password}")
  private String frontendResetPasswordUrl;
}
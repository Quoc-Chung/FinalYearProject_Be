package com.quocchung.cntt1.techcycle_system.dtos.response.Auth;

import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthTokenResponse {
  private String accessToken;
  private String tokenType;
  private Boolean isFirstLogin;
  private Long expiresIn;
  private UserResponse user;
}

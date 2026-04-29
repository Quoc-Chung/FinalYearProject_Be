package com.quocchung.cntt1.techcycle_system.dtos.response.Auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
  private Long userId;
  private String email;
  private String fullName;
  private Boolean isFirstRegister;
}

package com.quocchung.cntt1.techcycle_system.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
  private final Long userId;
  private final String email;
}

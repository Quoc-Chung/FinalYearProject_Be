package com.quocchung.cntt1.techcycle_system.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {
  private final Long userId;
  private final String email;

  @Override
  public String getName() {
    return email;
  }
}

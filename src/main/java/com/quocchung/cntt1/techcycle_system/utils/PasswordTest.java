package com.quocchung.cntt1.techcycle_system.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
  public static void main(String[] args) {
    System.out.println(
        new BCryptPasswordEncoder().encode("admin123")
    );
  }
}
package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import org.springframework.stereotype.Component;

@Component
public class Converter {

  public UserResponse mapToUserResponse(User user) {
    return UserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .bio(user.getBio())
        .avatarUrl(user.getAvatarUrl())
        .status(user.getStatus() != null ? user.getStatus().name() : null)
        .trustScore(user.getTrustScore())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .build();
  }

  public UserResponse mapResponse(User user) {
    return UserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .bio(user.getBio())
        .avatarUrl(user.getAvatarUrl())
        .status(user.getStatus() != null ? user.getStatus().name() : null)
        .trustScore(user.getTrustScore())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .build();
  }
}

package com.quocchung.cntt1.techcycle_system.dtos.response.User;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
  private Long userId;
  private String email;
  private String fullName;
  private String phone;
  private String bio;
  private String avatarUrl;
  private String status;
  private Boolean isFirstLogin;
  @Builder.Default
  private Double trustScore = 0.0;
  private String addressLine;
  private Long defaultAddressId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  private List<String> roleNames;
  private String bannedReason;
}

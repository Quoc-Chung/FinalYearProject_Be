package com.quocchung.cntt1.techcycle_system.dtos.response.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
  private Long userId;
  private String fullName;
  private String avatarUrl;
  private Double trustScore;
  private Long postCount;
  private Long followerCount;
  private Long followingCount;
}
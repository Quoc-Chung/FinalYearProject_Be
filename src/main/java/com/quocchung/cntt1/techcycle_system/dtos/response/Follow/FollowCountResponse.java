package com.quocchung.cntt1.techcycle_system.dtos.response.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowCountResponse {
  private Long userId;
  private long followerCount;
  private long followingCount;
}

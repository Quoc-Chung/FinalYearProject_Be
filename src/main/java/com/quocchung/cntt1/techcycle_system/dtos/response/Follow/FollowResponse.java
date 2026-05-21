package com.quocchung.cntt1.techcycle_system.dtos.response.Follow;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class FollowResponse {
  private Long followId;
  private UserSummaryResponse user;
  private String createdAt;
}

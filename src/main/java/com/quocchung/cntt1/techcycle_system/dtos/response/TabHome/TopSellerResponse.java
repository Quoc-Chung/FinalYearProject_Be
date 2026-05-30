package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopSellerResponse {
  private Long userId;
  private String fullName;
  private Integer countPost;
  private Long countFollower;
  private String avatarUrl;
}

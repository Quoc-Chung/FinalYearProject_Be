package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lấy ra những người chưa theo dõi . lấy ngẫu nhiên 3 thằng (ưu tiên những thằng có nhiều bài viết hoặc nhiều
// người theo dõi và chưa follow
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestedSellerResponse {
  private Long userId;
  private String fullName; // check nếu null thì lấy username
  private String countPost; // Số bài viết
  private String countFlow; // số người theo dõi
  private String avatarUrl;

}

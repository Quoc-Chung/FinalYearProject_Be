package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowCountResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowResponse;
import org.springframework.data.domain.Page;

public interface UserFollowService {
  // Follow một người dùng
  void followUser(Long currentUserId, Long targetUserId);

  // Unfollow một người dùng
  void unfollowUser(Long currentUserId, Long targetUserId);

  // Lấy danh sách người mình đang follow (following)
  Page<FollowResponse> getFollowing(Long userId, int page, int size);

  // Lấy danh sách người đang follow mình (followers)
  Page<FollowResponse> getFollowers(Long userId, int page, int size);

  // Đếm số follow / following
  FollowCountResponse getFollowCount(Long userId);
}

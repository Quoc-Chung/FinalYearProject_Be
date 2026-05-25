package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowCountResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.UserSummaryResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserFollow;
import com.quocchung.cntt1.techcycle_system.repository.UserFollowRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.service.UserFollowService;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

  private final UserFollowRepository userFollowRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  // FOLLOW
  @Override
  @Transactional
  public void followUser(Long currentUserId, Long targetUserId) {

    if (currentUserId.equals(targetUserId)) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "Không thể tự follow bản thân");
    }

    User follower = getUserOrThrow(currentUserId);
    User following = getUserOrThrow(targetUserId);

    // Kiểm tra đã follow chưa
    if (userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
      throw new ResException(ResErrorCode.ENTITY_EXISTED, "Bạn đã follow người dùng này rồi");
    }
    UserFollow userFollow = UserFollow.builder()
        .follower(follower)
        .following(following)
        .build();
    userFollowRepository.save(userFollow);

    notificationService.createNotification(
        following,
        follower,
        NotificationType.USER_FOLLOWED,
        follower.getFullName() + " đã theo dõi bạn",
        follower.getFullName() + " đã bắt đầu theo dõi bạn trên Techcycle",
        "/profile/" + follower.getUserId(),
        Map.of("followerId", follower.getUserId())
    );
  }
  // UNFOLLOW
  @Override
  @Transactional
  public void unfollowUser(Long currentUserId, Long targetUserId) {
    if (currentUserId.equals(targetUserId)) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "Không thể tự unfollow bản thân");
    }

    User follower = getUserOrThrow(currentUserId);
    User following = getUserOrThrow(targetUserId);

    UserFollow userFollow = userFollowRepository
        .findByFollowerAndFollowing(follower, following)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Bạn chưa follow người dùng này"));

    userFollowRepository.delete(userFollow);
  }

  // DANH SÁCH FOLLOWING (người mình đang follow)
  @Override
  public Page<FollowResponse> getFollowing(Long userId, int page, int size) {
    getUserOrThrow(userId); // check user tồn tại

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    Page<UserFollow> follows = userFollowRepository.findFollowingByFollowerId(userId, pageable);

    return follows.map(uf -> FollowResponse.builder()
        .followId(uf.getFollowId())
        .user(toUserSummary(uf.getFollowing()))
        .createdAt(uf.getCreatedAt().toString())
        .build());
  }

  // DANH SÁCH FOLLOWERS (người đang follow mình)
  @Override
  public Page<FollowResponse> getFollowers(Long userId, int page, int size) {
    getUserOrThrow(userId);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    Page<UserFollow> follows = userFollowRepository.findFollowersByFollowingId(userId, pageable);

    return follows.map(uf -> FollowResponse.builder()
        .followId(uf.getFollowId())
        .user(toUserSummary(uf.getFollower()))
        .createdAt(uf.getCreatedAt().toString())
        .build());
  }

  // ĐẾM SỐ FOLLOW
  @Override
  public FollowCountResponse getFollowCount(Long userId) {
    getUserOrThrow(userId);

    long followerCount = userFollowRepository.countFollowersByUserId(userId);
    long followingCount = userFollowRepository.countFollowingByUserId(userId);

    return FollowCountResponse.builder()
        .userId(userId)
        .followerCount(followerCount!= 0 ? followerCount : 0)
        .followingCount(followingCount!=0 ? followingCount : 0)
        .build();
  }

  private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));
  }

  private UserSummaryResponse toUserSummary(User user) {
    return UserSummaryResponse.builder()
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .avatarUrl(user.getAvatarUrl())
        .trustScore(user.getTrustScore())
        .build();
  }


}

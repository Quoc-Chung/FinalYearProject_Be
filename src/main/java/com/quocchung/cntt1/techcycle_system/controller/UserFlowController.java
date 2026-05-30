package com.quocchung.cntt1.techcycle_system.controller;


import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowCountResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Follow.FollowResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.UserFollowService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-follow")
@RequiredArgsConstructor
public class UserFlowController {

  private final ResponseUtils responseUtils;
  private final UserFollowService userFollowService;

  // Follow một người dùng
  @PostMapping("/{targetUserId}")
  public ResponseEntity<APIResponse<Void>> followUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long targetUserId) {
    userFollowService.followUser(userPrincipal.getUserId(), targetUserId);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  // Unfollow một người dùng
  @DeleteMapping("/{targetUserId}")
  public ResponseEntity<APIResponse<Void>> unfollowUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long targetUserId) {
    userFollowService.unfollowUser(userPrincipal.getUserId(), targetUserId);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  // Danh sách người mà mình đang follow
  @GetMapping("/following")
  public ResponseEntity<APIResponse<FollowResponse>> getFollowing(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<FollowResponse> result = userFollowService.getFollowing(userPrincipal.getUserId(), page,
        size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
  }

  // Danh sách người đang follow mình
  @GetMapping("/followers")
  public ResponseEntity<APIResponse<FollowResponse>> getFollowers(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<FollowResponse> result = userFollowService.getFollowers(userPrincipal.getUserId(), page,
        size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
  }

  // Đếm số lượng người mình follow và người đang follow mình
  @GetMapping("/follow-count")
  public ResponseEntity<APIResponse<FollowCountResponse>> getFollowCount(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    FollowCountResponse result = userFollowService.getFollowCount(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(result));
  }

  // Kiểm tra currentUser có đang follow targetUser không
  @GetMapping("/is-following/{targetUserId}")
  public ResponseEntity<APIResponse<Boolean>> isFollowing(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long targetUserId) {
    boolean result = userFollowService.isFollowing(userPrincipal.getUserId(), targetUserId);
    return ResponseEntity.ok(responseUtils.success(result));
  }
}

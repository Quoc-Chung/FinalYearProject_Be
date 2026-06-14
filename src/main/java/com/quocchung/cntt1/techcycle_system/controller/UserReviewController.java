package com.quocchung.cntt1.techcycle_system.controller;


import com.quocchung.cntt1.techcycle_system.dtos.request.Review.ReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.ReviewResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.UserReviewService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-reviews")
@RequiredArgsConstructor
public class UserReviewController {

  private final UserReviewService userReviewService;
  private final ResponseUtils responseUtils;

  @PostMapping("/review/{toUserId}")
  public ResponseEntity<APIResponse<ReviewResponse>> reviewUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long toUserId,
      @Valid @RequestBody ReviewRequest request) {
    ReviewResponse result = userReviewService.reviewUser(userPrincipal.getUserId(), toUserId, request);
    return ResponseEntity.ok(responseUtils.success(result));
  }

  @GetMapping("/me")
  public ResponseEntity<APIResponse<ReviewResponse>> getMyReviews(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<ReviewResponse> result = userReviewService.getReviews(userPrincipal.getUserId(), page, size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), result.getNumber() + 1, result.getTotalElements(), size));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<APIResponse<ReviewResponse>> getUserReviews(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<ReviewResponse> result = userReviewService.getReviews(userId, page, size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), result.getNumber() + 1, result.getTotalElements(), size));
  }

  @GetMapping("/has-transaction/{userId}")
  public ResponseEntity<APIResponse<Boolean>> hasTransactionWithUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Long userId) {
    boolean hasTransaction = userReviewService.hasTransactionWithUser(
        userPrincipal.getUserId(), userId);
    return ResponseEntity.ok(responseUtils.success(hasTransaction));
  }

}

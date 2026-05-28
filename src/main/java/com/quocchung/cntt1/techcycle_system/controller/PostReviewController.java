package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Review.PostReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.PostReviewResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.PostReviewService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post-reviews")
@RequiredArgsConstructor
public class PostReviewController {

    private final PostReviewService postReviewService;
    private final ResponseUtils responseUtils;

    @PostMapping("/post/{postId}")
    public ResponseEntity<APIResponse<PostReviewResponse>> createReview(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PostReviewRequest request) {
        PostReviewResponse result = postReviewService.createReview(
                postId, userPrincipal.getUserId(), request);
        return ResponseEntity.ok(responseUtils.success(result));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<APIResponse<PostReviewResponse>> getReviewsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostReviewResponse> result = postReviewService.getReviewsByPostId(postId, page, size);
        return ResponseEntity.ok(
                responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<APIResponse<PostReviewResponse>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostReviewResponse> result = postReviewService.getReviewsByUserId(userId, page, size);
        return ResponseEntity.ok(
                responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/post/{postId}/stats")
    public ResponseEntity<APIResponse<PostReviewResponse>> getPostReviewStats(
            @PathVariable Long postId) {
        PostReviewResponse result = postReviewService.getReviewStats(postId);
        return ResponseEntity.ok(responseUtils.success(result));
    }
}

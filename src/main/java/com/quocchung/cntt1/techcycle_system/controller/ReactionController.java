package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Reaction.ReactionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.PostReactionReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ReactionService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {
  private final ReactionService reactionService;
  private final ResponseUtils responseUtils;

  /**
   * Add reaction in port
   * @param postId
   * @param request
   * @param userPrincipal
   * @return
   */
  @PostMapping("/post/{postId}")
  public ResponseEntity<APIResponse<ReactionResponse>> addReaction(
      @PathVariable Long postId,
      @Valid @RequestBody ReactionRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ReactionResponse response = reactionService.addReaction(
        postId, request, userPrincipal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseUtils.success(response));
  }

  /**
   *Update reaction in port
   * @param postId
   * @param request
   * @param userPrincipal
   * @return
   */
  @PutMapping("/post/{postId}")
  public ResponseEntity<APIResponse<ReactionResponse>> updateReaction(
      @PathVariable Long postId,
      @Valid @RequestBody ReactionRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    ReactionResponse response = reactionService.updateReaction(
        postId, request, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(response));
  }
  /**
   * remove reaction
   * @param postId
   * @param userPrincipal
   * @return
   */
  @DeleteMapping("/post/{postId}")
  public ResponseEntity<APIResponse<Void>> removeReaction(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    reactionService.removeReaction(postId, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  /**
   *  Lấy tất cả reactions của một bài viết cụ thể
   * @param postId
   * @param userPrincipal
   * @return
   */
  @GetMapping("/post/{postId}")
  public ResponseEntity<APIResponse<ReactionResponse>> getReactionsByPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long currentUserId = userPrincipal != null ? userPrincipal.getUserId() : null;
    ReactionResponse response = reactionService.getReactionsByPost(postId, currentUserId);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  /**
   * Lấy danh sách tất cả reactions mà user đã thả trên các bài viết.
   * @param userPrincipal
   * @return
   */
  @GetMapping("/user/me")
  public ResponseEntity<APIResponse<List<ReactionResponse>>> getMyReactions(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    List<ReactionResponse> responses = reactionService.getUserReactions(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(responses));
  }

  /**
   * Lấy báo cáo chi tiết reactions của một bài viết
   * @param postId
   * @return
   */
  @GetMapping("/post/{postId}/report")
  public ResponseEntity<APIResponse<PostReactionReportResponse>> getPostReactionReport(
      @PathVariable Long postId
  ) {
    PostReactionReportResponse response = reactionService.getPostReactionReport(postId);
    return ResponseEntity.ok(responseUtils.success(response));
  }
}

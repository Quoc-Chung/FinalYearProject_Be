package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Reaction.ReactionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.PostReactionReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionCountItem;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ReactionService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  @DeleteMapping("/post/{postId}")
  public ResponseEntity<APIResponse<Void>> removeReaction(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    reactionService.removeReaction(postId, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<APIResponse<ReactionResponse>> getReactionsByPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long currentUserId = userPrincipal != null ? userPrincipal.getUserId() : null;
    ReactionResponse response = reactionService.getReactionsByPost(postId, currentUserId);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  @GetMapping("/user/me")
  public ResponseEntity<APIResponse<List<ReactionResponse>>> getMyReactions(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    List<ReactionResponse> responses = reactionService.getUserReactions(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(responses));
  }

  @GetMapping("/post/{postId}/report")
  public ResponseEntity<APIResponse<PostReactionReportResponse>> getPostReactionReport(
      @PathVariable Long postId
  ) {
    PostReactionReportResponse response = reactionService.getPostReactionReport(postId);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  /**
   * Lấy reaction counts cho nhiều bài viết cùng lúc
   * GET /api/reactions/posts/counts?postIds=1,2,3
   */
  @GetMapping("/posts/counts")
  public ResponseEntity<APIResponse<ReactionCountItem>> getReactionsCountsForPosts(
      @RequestParam String postIds
  ) {
    List<Long> ids = Arrays.stream(postIds.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::parseLong)
        .collect(Collectors.toList());
    Map<Long, Map<String, Object>> result = reactionService.getReactionsCountsForPosts(ids);
    List<ReactionCountItem> countsList = result.entrySet().stream()
        .map(e -> ReactionCountItem.builder()
            .postId(e.getKey())
            .totalReactions(((Number) e.getValue().get("totalReactions")).longValue())
            .topReaction((String) e.getValue().get("topReaction"))
            .build())
        .collect(Collectors.toList());
    return ResponseEntity.ok(responseUtils.successList(countsList));
  }
}

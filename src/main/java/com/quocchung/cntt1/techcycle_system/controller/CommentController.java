package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Comment.CommentRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Comment.CommentResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.CommentService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final ResponseUtils responseUtils;

  @PostMapping
  public ResponseEntity<APIResponse<CommentResponse>> createComment(
      @Valid @RequestBody CommentRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    CommentResponse response = commentService.createComment(request, userPrincipal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseUtils.success(response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<APIResponse<CommentResponse>> updateComment(
      @PathVariable Long id,
      @Valid @RequestBody CommentRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    CommentResponse response = commentService.updateComment(id, request, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<APIResponse<Void>> deleteComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.deleteComment(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PutMapping("/{id}/hide")
  public ResponseEntity<APIResponse<Void>> hideComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.hideComment(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PutMapping("/{id}/show")
  public ResponseEntity<APIResponse<Void>> showComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.showComment(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PutMapping("/{id}/pin")
  public ResponseEntity<APIResponse<Void>> pinComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.pinComment(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PutMapping("/{id}/unpin")
  public ResponseEntity<APIResponse<Void>> unpinComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.unpinComment(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @PostMapping("/{id}/react")
  public ResponseEntity<APIResponse<Void>> reactToComment(
      @PathVariable Long id,
      @RequestParam ReactionType reactionType,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.reactToComment(id, reactionType, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @DeleteMapping("/{id}/react")
  public ResponseEntity<APIResponse<Void>> removeReaction(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    commentService.removeReaction(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<APIResponse<CommentResponse>> getComment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    CommentResponse response = commentService.getCommentById(id, userId);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<APIResponse<List<CommentResponse>>> getCommentsByPost(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<CommentResponse> responses = commentService.getCommentsByPostId(postId, userId);
    return ResponseEntity.ok(responseUtils.success(responses));
  }

  @GetMapping("/post/{postId}/count")
  public ResponseEntity<APIResponse<Long>> countCommentsByPost(@PathVariable Long postId) {
    long count = commentService.countCommentsByPostId(postId);
    return ResponseEntity.ok(responseUtils.success(count));
  }
}

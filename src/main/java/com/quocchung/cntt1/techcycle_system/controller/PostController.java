package com.quocchung.cntt1.techcycle_system.controller;


import com.quocchung.cntt1.techcycle_system.dtos.request.Post.CreatePostRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;

import com.quocchung.cntt1.techcycle_system.service.PostService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
  private final PostService postService;
  private final ResponseUtils responseUtils;

  @PostMapping
  public ResponseEntity<APIResponse<PostResponse>> createPost(
      @Valid @RequestBody CreatePostRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    PostResponse response = postService.createPost(request, userPrincipal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseUtils.success(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<APIResponse<PostResponse>> getPost(@PathVariable Long id) {
    return ResponseEntity.ok(responseUtils.success(postService.getPost(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<APIResponse<PostResponse>> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody CreatePostRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    return ResponseEntity.ok(responseUtils.success(
        postService.updatePost(id, request, userPrincipal.getUserId())));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<APIResponse<Void>> deletePost(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    postService.deletePost(id, userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(null));
  }

  @GetMapping("/page")
  public ResponseEntity<APIResponse<PostResponse>> getPostsPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    int pageIndex = Math.max(1, page) - 1;
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    Page<PostResponse> result = postService.getPostsPage(pageable, userId);
    return ResponseEntity.ok(responseUtils.successPage(
        result.getContent(), result.getNumber() + 1, result.getTotalElements(), result.getSize()));
  }

  @GetMapping("/all-post-data")
  public ResponseEntity<APIResponse<List<PostResponse>>> getApprovedPosts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) String ward,
      @RequestParam(required = false) String province,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<PostResponse> posts = postService.getApprovedPosts(keyword, categoryId, brandId, ward, province, userId);
    return ResponseEntity.ok(responseUtils.success(posts));
  }

  @GetMapping("/search")
  public ResponseEntity<APIResponse<List<PostResponse>>> searchPosts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String authorName,
      @RequestParam(required = false) String address,
      @RequestParam(required = false) String province,
      @RequestParam(required = false) String ward,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(required = false) String tag
  ) {
    List<PostResponse> result = postService.searchPostsNoPage(
        keyword, title, description, authorName, address, province, ward,
        categoryId, brandId, minPrice, maxPrice, tag);
    return ResponseEntity.ok(responseUtils.success(result));
  }

  @GetMapping("/my-posts")
  public ResponseEntity<APIResponse<PostResponse>> getMyPosts(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    int pageIndex = Math.max(1, page) - 1;
    Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<PostResponse> result = postService.getMyPosts(userPrincipal.getUserId(), pageable);
    return ResponseEntity.ok(responseUtils.successPage(
        result.getContent(), result.getNumber() + 1, result.getTotalElements(), result.getSize()));
  }

  @PostMapping("/approve/{id}")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ADMIN')")
  public ResponseEntity<APIResponse<PostResponse>> approvePost(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    return ResponseEntity.ok(responseUtils.success(
        postService.approvePost(id, userPrincipal.getUserId())));
  }

  @PostMapping("/reject/{id}")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ADMIN')")
  public ResponseEntity<APIResponse<PostResponse>> rejectPost(
      @PathVariable Long id,
      @RequestParam String reason,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    return ResponseEntity.ok(responseUtils.success(
        postService.rejectPost(id, reason, userPrincipal.getUserId())));
  }

  @GetMapping("/get-latest-posts")
  public ResponseEntity<APIResponse<List<PostResponse>>> getLatestPosts(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<PostResponse> posts = postService.getLatestPosts(userId);
    return ResponseEntity.ok(responseUtils.success(posts));
  }

  @GetMapping("/search-category")
  public ResponseEntity<APIResponse<List<PostResponse>>> searchPostsByCategory(
      @RequestParam(required = false) Long categoryId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<PostResponse> result = postService.searchPostsByCategory(categoryId, userId);
    return ResponseEntity.ok(responseUtils.success(result));
  }
}

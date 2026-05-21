package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.SavePost.SavePostResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.SavePostService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/save-post")
public class SavePostController {

  private final SavePostService savePostService;
  private final ResponseUtils responseUtils;

  // Lưu một bài đăng
  @PostMapping("/save")
  public ResponseEntity<APIResponse<SavePostResponse>> savePost(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam Long postId,
      @RequestParam Long collectionId

  ) {
    SavePostResponse result = savePostService.savePort(userPrincipal.getUserId(), postId, collectionId);
    return ResponseEntity.ok(responseUtils.success(result));
  }
  // Bo luu mot bai dang khoi collection
  @PostMapping("/un-save")
  public ResponseEntity<APIResponse<Void>> unSavePost(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam Long postId,
      @RequestParam Long collectionId

  ) {
    savePostService.unSavePort(userPrincipal.getUserId(), postId, collectionId);
    return ResponseEntity.ok(responseUtils.success(null));
  }

  // Lấy danh sách các bài đăng đã lưu
  @GetMapping("/get-all-by-save=post")
  public ResponseEntity<APIResponse<List<PostResponse>>> getAllSavePortByCollection(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam Long collectionId
  ) {
    List<PostResponse> result = savePostService.getAllSavePostByCollection(userPrincipal.getUserId(), collectionId);
    return ResponseEntity.ok(responseUtils.success(result));
  }

}


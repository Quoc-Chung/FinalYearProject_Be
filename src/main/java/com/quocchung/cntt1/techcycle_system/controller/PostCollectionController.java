package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.PostCollection.PostCollectionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.PostCollectionResponse.PostCollectionResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.PostCollectionService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post-collection")
@RequiredArgsConstructor
public class PostCollectionController {
   private final PostCollectionService postCollectionService;
   private final ResponseUtils responseUtils;

   // them
   @PostMapping("/add")
   public ResponseEntity<APIResponse< PostCollectionResponse>> addPostCollection(
       @RequestBody PostCollectionRequest addPostCollectionRequest,
       @AuthenticationPrincipal UserPrincipal userPrincipal
   ) {
      Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
      PostCollectionResponse result =postCollectionService.addPostCollection(userId,addPostCollectionRequest);
      return ResponseEntity.ok(responseUtils.success(result));
   }  

   // sua
   @PutMapping("/update/{id}")
   public ResponseEntity<APIResponse< PostCollectionResponse>> updatePostCollection(
       @PathVariable Long id,
       @RequestBody PostCollectionRequest addPostCollectionRequest,
       @AuthenticationPrincipal UserPrincipal userPrincipal
   ) {
      Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
      PostCollectionResponse result =postCollectionService.updatePostCollection(id, userId,addPostCollectionRequest);
      return ResponseEntity.ok(responseUtils.success(result));
   }

   // xoa thì xoa luon bai dang duoc luu trong nay
   @DeleteMapping("/delete/{id}")
   public ResponseEntity<APIResponse<Void>> deletePostCollection(
       @PathVariable Long id,
       @AuthenticationPrincipal UserPrincipal userPrincipal
   ) {
      Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
      postCollectionService.deletePostCollection(userId,id);
      return ResponseEntity.ok(responseUtils.success(null));
   }

   // láy danh sach cac collection cua mot user
   @GetMapping("/list")
   public ResponseEntity<APIResponse<List<PostCollectionResponse>>> getAll(

       @AuthenticationPrincipal UserPrincipal userPrincipal
   ) {
      Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
      List<PostCollectionResponse> result =postCollectionService.getAll(userId);
      return ResponseEntity.ok(responseUtils.success(result));
   }

}

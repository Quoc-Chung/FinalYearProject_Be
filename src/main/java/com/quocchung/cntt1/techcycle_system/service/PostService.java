package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Post.CreatePostRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostDetailUser;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
  PostResponse createPost(CreatePostRequest request, Long userId);

  PostResponse getPost(Long id);

  PostResponse updatePost(Long id, CreatePostRequest request, Long userId);

  void deletePost(Long id, Long userId);

  Page<PostResponse> getPostsPage(Pageable pageable);

  List<PostResponse> getApprovedPosts(String keyword, Long categoryId, Long brandId, String ward, String province);

  Page<PostResponse> getMyPosts(Long userId, Pageable pageable);

  Page<PostResponse> searchPosts(
      String keyword,
      String title,
      String description,
      String authorName,
      String address,
      String province,
      String ward,
      Long categoryId,
      Long brandId,
      Double minPrice,
      Double maxPrice,
      String tag,
      Pageable pageable
  );

  List<PostResponse> searchPostsNoPage(
      String keyword,
      String title,
      String description,
      String authorName,
      String address,
      String province,
      String ward,
      Long categoryId,
      Long brandId,
      Double minPrice,
      Double maxPrice,
      String tag
  );

  PostResponse approvePost(Long postId, Long adminId);

  PostResponse rejectPost(Long postId, String reason, Long adminId);

  List<PostResponse> getLatestPosts();

  List<PostResponse> searchPostsByCategory(Long categoryId);

  // Methods with userId for current user reaction
  Page<PostResponse> getPostsPage(Pageable pageable, Long userId);

  List<PostResponse> getApprovedPosts(String keyword, Long categoryId, Long brandId, String ward, String province, Long userId);

  List<PostResponse> getLatestPosts(Long userId);

  List<PostResponse> searchPostsByCategory(Long categoryId, Long userId);

  PostDetailUser getPostDetailUser(Long postId);

  List<PostResponse> hotPost();

  List<PostResponse> getMyPostsByStatus(Long userId, PostStatus status);

  PostResponse hidePost(Long postId, Long userId);

  PostResponse markAsSold(Long postId, Long userId);

  PostResponse unhidePost(Long postId, Long userId);
}

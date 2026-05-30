package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse.PostImageInfo;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.PostUserFollowResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SearchPopularResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SuggestedSellerResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.TodayActivityResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.NewestPostSidebarResponse;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserFollow;
import com.quocchung.cntt1.techcycle_system.repository.CommentRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostViewRepository;
import com.quocchung.cntt1.techcycle_system.repository.SearchHistoryRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserFollowRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.TabHomeService;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TabHomeServiceImpl implements TabHomeService {

  private final UserRepository userRepository;
  private final UserFollowRepository userFollowRepository;
  private final PostRepository postRepository;
  private final PostReactionRepository postReactionRepository;
  private final CommentRepository commentRepository;
  private final PostViewRepository postViewRepository;
  private final SearchHistoryRepository searchHistoryRepository;
  private final MinioProperties minioProperties;

  @Override
  public List<PostUserFollowResponse> getPostsByFollowedUsers(Long userId) {
    if (userId == null) {
      return Collections.emptyList();
    }

    List<UserFollow> followed = userFollowRepository
        .findFollowingByFollowerId(userId, PageRequest.of(0, 100))
        .getContent();

    if (followed.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> followingIds = followed.stream()
        .map(f -> f.getFollowing().getUserId())
        .collect(Collectors.toList());

    List<Post> posts = postRepository.findAll().stream()
        .filter(p -> p.getStatus() == PostStatus.APPROVED)
        .filter(p -> followingIds.contains(p.getUser().getUserId()))
        .sorted((a, b) -> {
          LocalDateTime timeA = a.getCreatedAt();
          LocalDateTime timeB = b.getCreatedAt();
          if (timeA == null && timeB == null) return 0;
          if (timeA == null) return 1;
          if (timeB == null) return -1;
          return timeB.compareTo(timeA);
        })
        .limit(20)
        .collect(Collectors.toList());

    if (posts.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> postIds = posts.stream().map(Post::getPostId).collect(Collectors.toList());

    Map<Long, Long> reactionCounts = postReactionRepository.countReactionsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    Map<Long, Long> commentCounts = commentRepository.countCommentsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    return posts.stream().map(post -> {
      String fullName = post.getUser().getFullName();
      if (fullName == null || fullName.isBlank()) {
        fullName = post.getUser().getEmail();
      }

      String address = "";
      if (post.getAddress() != null) {
        String ward = post.getAddress().getWard();
        String province = post.getAddress().getProvince();
        address = (ward != null ? ward : "") + ", " + (province != null ? province : "");
        address = address.trim();
        if (address.startsWith(",") || address.equals(", ")) {
          address = "";
        }
      }

      List<PostImageInfo> imageInfos = post.getImages().stream()
          .sorted((a, b) -> {
            if (a.getSortOrder() == null && b.getSortOrder() == null) return 0;
            if (a.getSortOrder() == null) return 1;
            if (b.getSortOrder() == null) return -1;
            return a.getSortOrder().compareTo(b.getSortOrder());
          })
          .map(img -> PostImageInfo.builder()
              .imageId(img.getImageId())
              .publicUrl(buildPublicUrl(img.getObjectKey()))
              .thumbnailUrl(img.getThumbnailKey() != null
                  ? buildPublicUrl(img.getThumbnailKey()) : null)
              .mediaType(img.getMediaType())
              .sortOrder(img.getSortOrder())
              .width(img.getWidth())
              .height(img.getHeight())
              .duration(img.getDuration())
              .build())
          .collect(Collectors.toList());

      return PostUserFollowResponse.builder()
          .postId(post.getPostId())
          .userId(post.getUser().getUserId())
          .fullName(fullName)
          .avatarUrl(post.getUser().getAvatarUrl())
          .address(address)
          .title(post.getTitle())
          .countReaction(reactionCounts.getOrDefault(post.getPostId(), 0L))
          .countComment(commentCounts.getOrDefault(post.getPostId(), 0L))
          .price(post.getPrice() != null ? post.getPrice().longValue() : 0L)
          .images(imageInfos)
          .build();
    }).collect(Collectors.toList());
  }

  @Override
  public List<SearchPopularResponse> getPopularSearches() {
    return searchHistoryRepository.findAll().stream()
        .collect(Collectors.groupingBy(
            sh -> sh.getKeyword().toLowerCase().trim(),
            Collectors.counting()
        ))
        .entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(10)
        .map(e -> SearchPopularResponse.builder()
            .keyword(e.getKey())
            .resultCount(e.getValue().intValue())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  public List<SuggestedSellerResponse> getSuggestedSellers(Long userId) {
    List<User> allUsers = userRepository.findAllByRoleName("USER");

    List<Long> excludedIds = allUsers.stream()
        .filter(u -> u.getUserId().equals(userId))
        .findFirst()
        .map(excludeUser -> {
          List<UserFollow> following = userFollowRepository
              .findFollowingByFollowerId(excludeUser.getUserId(), PageRequest.of(0, 1000))
              .getContent();
          return following.stream()
              .map(f -> f.getFollowing().getUserId())
              .collect(Collectors.toList());
        })
        .orElse(Collections.emptyList());

    excludedIds.add(userId);

    List<User> candidates = allUsers.stream()
        .filter(u -> !excludedIds.contains(u.getUserId()))
        .collect(Collectors.toList());

    List<SuggestedSellerResponse> sellers = candidates.stream().map(user -> {
      long countPost = postRepository.countByUserUserId(user.getUserId());
      long countFollow = userFollowRepository.countFollowersByUserId(user.getUserId());
      boolean isFollowing = userFollowRepository
          .existsByFollowerUserIdAndFollowingUserId(userId, user.getUserId());
      return SuggestedSellerResponse.builder()
          .userId(user.getUserId())
          .fullName(user.getFullName() != null ? user.getFullName() : user.getEmail())
          .countPost(String.valueOf(countPost))
          .countFlow(String.valueOf(countFollow))
          .avatarUrl(user.getAvatarUrl())
          .isFollowing(isFollowing)
          .build();
    }).collect(Collectors.toList());

    return sellers.stream()
        .sorted((a, b) -> {
          int postsA = Integer.parseInt(a.getCountPost());
          int postsB = Integer.parseInt(b.getCountPost());
          int followsA = Integer.parseInt(a.getCountFlow());
          int followsB = Integer.parseInt(b.getCountFlow());
          long scoreA = (long) postsA * 10 + followsA;
          long scoreB = (long) postsB * 10 + followsB;
          return Long.compare(scoreB, scoreA);
        })
        .limit(3)
        .collect(Collectors.toList());
  }

  @Override
  public TodayActivityResponse getTodayActivity() {
    LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);

    // Lấy tất cả posts để kiểm tra approvedAt và updatedAt
    List<Post> allPosts = postRepository.findAll();

    // Đếm các bài được tạo hôm nay
    long countPostToday = allPosts.stream()
        .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfDay))
        .count();

    // Đếm các bài được duyệt hôm nay (approvedAt hôm nay, không phụ thuộc ngày tạo)
    long countApproved = allPosts.stream()
        .filter(p -> p.getApprovedAt() != null && p.getApprovedAt().isAfter(startOfDay))
        .count();

    // Đếm các bài bị từ chối hôm nay (updatedAt hôm nay và status là REJECTED)
    long countRejected = allPosts.stream()
        .filter(p -> p.getStatus() != null && p.getStatus().equals(PostStatus.REJECTED))
        .filter(p -> p.getUpdatedAt() != null && p.getUpdatedAt().isAfter(startOfDay))
        .count();

    long newUserCount = userRepository.findAll().stream()
        .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfDay))
        .count();

    long reactionCount = postReactionRepository.findAll().stream()
        .filter(pr -> pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(startOfDay))
        .count();

    long visitCount = postViewRepository.countViewsSince(startOfDay);

    return TodayActivityResponse.builder()
        .countPort(countPostToday)
        .newUser(newUserCount)
        .countReaction(reactionCount)
        .numberOfVisits(visitCount)
        .countApproved(countApproved)
        .countRejected(countRejected)
        .build();
  }

  private String buildPublicUrl(String objectKey) {
    String base = minioProperties.getPublicEndpoint();
    if (base == null || base.isBlank()) {
      base = minioProperties.getEndpoint();
    }
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/" + minioProperties.getBucketName() + "/" + objectKey;
  }

  @Override
  public List<NewestPostSidebarResponse> getNewestPostsForSidebar(int limit) {
    LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);

    List<Post> posts = postRepository.findAll().stream()
        .filter(p -> p.getStatus() == PostStatus.APPROVED)
        .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfDay))
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .limit(limit)
        .collect(Collectors.toList());

    if (posts.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> postIds = posts.stream().map(Post::getPostId).collect(Collectors.toList());

    Map<Long, Long> reactionCounts = postReactionRepository.countReactionsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    Map<Long, Long> commentCounts = commentRepository.countCommentsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    return posts.stream().map(post -> {
      String thumbnailUrl = null;
      String mediaType = null;
      if (post.getImages() != null && !post.getImages().isEmpty()) {
        PostImage firstImage = post.getImages().get(0);
        thumbnailUrl = buildPublicUrl(firstImage.getObjectKey());
        mediaType = firstImage.getMediaType() != null ? firstImage.getMediaType().name() : "IMAGE";
      }

      String formattedPrice = post.getPrice() != null
          ? currencyFormat.format(post.getPrice()).replace("₫", "") + "₫"
          : "Liên hệ";

      return NewestPostSidebarResponse.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .price(post.getPrice() != null ? post.getPrice().longValue() : 0L)
          .formattedPrice(formattedPrice)
          .thumbnailUrl(thumbnailUrl)
          .mediaType(mediaType)
          .userId(post.getUser().getUserId())
          .userFullName(post.getUser().getFullName())
          .userAvatarUrl(post.getUser().getAvatarUrl())
          .countReaction(reactionCounts.getOrDefault(post.getPostId(), 0L))
          .countComment(commentCounts.getOrDefault(post.getPostId(), 0L))
          .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : "")
          .relativeTime(formatRelativeTime(post.getCreatedAt()))
          .build();
    }).collect(Collectors.toList());
  }

  private String formatRelativeTime(LocalDateTime dateTime) {
    if (dateTime == null) return "Vừa xong";
    LocalDateTime now = LocalDateTime.now();
    long minutes = java.time.Duration.between(dateTime, now).toMinutes();
    long hours = java.time.Duration.between(dateTime, now).toHours();
    long days = java.time.Duration.between(dateTime, now).toDays();
    if (minutes < 1) return "Vừa xong";
    if (minutes < 60) return minutes + " phút trước";
    if (hours < 24) return hours + " giờ trước";
    return days + " ngày trước";
  }

}

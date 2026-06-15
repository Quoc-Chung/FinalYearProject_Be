package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard.CategoryStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.FullReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Category;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.CategoryRepository;
import com.quocchung.cntt1.techcycle_system.repository.CommentRepository;
import com.quocchung.cntt1.techcycle_system.repository.DashboardRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReportRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostViewRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserFollowRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.service.ReportService;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private static final long AUTO_HIDE_THRESHOLD = 3L;
  private static final int VIOLATION_LOOKBACK_DAYS = 30;

  private final PostReportRepository postReportRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final PostReactionRepository postReactionRepository;
  private final PostViewRepository postViewRepository;
  private final UserFollowRepository userFollowRepository;
  private final DashboardRepository dashboardRepository;
  private final CategoryRepository categoryRepository;
  private final MinioProperties minioProperties;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
            "Không tìm thấy bài đăng với id: " + postId));

    if (post.getStatus() != PostStatus.APPROVED) {
      throw new ResException(ResErrorCode.INVALID_REQUEST,
          "Chỉ có thể báo cáo bài đăng đang ở trạng thái APPROVED");
    }

    if (post.getUser().getUserId().equals(reporterId)) {
      throw new ResException(ResErrorCode.INVALID_REQUEST,
          "Bạn không thể tự báo cáo bài đăng của chính mình");
    }

    if (postReportRepository.existsByPostPostIdAndReporterUserId(postId, reporterId)) {
      throw new ResException(ResErrorCode.INVALID_REQUEST,
          "Bạn đã báo cáo bài đăng này rồi");
    }

    User reporter = userRepository.findById(reporterId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + reporterId));

    PostReport report = PostReport.builder()
        .post(post)
        .reporter(reporter)
        .reason(request.getReason().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .status(ReportStatus.PENDING)
        .build();

    report = postReportRepository.save(report);

    long pendingReporterCount = postReportRepository.countDistinctReporterByPostIdAndStatus(
        postId, ReportStatus.PENDING);

    // Notify post owner
    if (pendingReporterCount >= AUTO_HIDE_THRESHOLD && post.getStatus() == PostStatus.APPROVED) {
      post.setStatus(PostStatus.HIDDEN);
      postRepository.save(post);
      notifyPostOwnerReported(post, reporter, true, pendingReporterCount);
    } else {
      notifyPostOwnerReported(post, reporter, false, pendingReporterCount);
    }

    // Notify all admins about new report
    notifyAdminsAboutNewReport(post, reporter);

    return toReportResponse(report);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReportResponse> getReportsByPost(Long postId, int page, int size) {
    postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
            "Không tìm thấy bài đăng với id: " + postId));

    Pageable pageable = PageRequest.of(page - 1, size);
    return postReportRepository.findByPostPostIdOrderByCreatedAtDesc(postId, pageable)
        .map(this::toReportResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return postReportRepository.findByReporterUserId(reporterId, pageable)
        .map(this::toReportResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return postReportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
        .map(this::toReportResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReportResponse> getAdminReports(ReportStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<PostReport> reports = status == null
        ? postReportRepository.findAllForAdmin(pageable)
        : postReportRepository.findAllForAdminByStatus(status, pageable);
    return new PageImpl<>(reports.getContent().stream().map(this::toReportResponse).toList(),
        pageable, reports.getTotalElements());
  }

  @Override
  @Transactional
  public void resolvePostReports(Long postId, Long adminId, boolean isViolation) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND, "Không tìm thấy bài"));

    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND, "Không tìm thấy admin"));

    List<PostReport> pendingReports = postReportRepository.findByPostPostIdAndStatus(postId, ReportStatus.PENDING);
    if (pendingReports.isEmpty()) {
      throw new ResException(ResErrorCode.INVALID_REQUEST, "Bài viết này không còn báo cáo chờ xử lý");
    }

    ReportStatus reportStatus = isViolation ? ReportStatus.APPROVED : ReportStatus.REJECTED;
    LocalDateTime resolvedAt = LocalDateTime.now();
    postReportRepository.resolvePendingReportsByPostId(postId, reportStatus, admin, resolvedAt);

    if (isViolation) {
      post.setStatus(PostStatus.VIOLATION);
      postRepository.save(post);
      applyViolationPolicy(post.getUser());
      notifyPostOwnerModerated(post, true);
    } else {
      post.setStatus(PostStatus.APPROVED);
      postRepository.save(post);
      notifyPostOwnerModerated(post, false);
    }
  }

  @Override
  public boolean checkUserReportedPost(Long postId, Long userId) {
    if (userId == null) {
      return false;
    }
    return postReportRepository.existsByPostPostIdAndReporterUserId(postId, userId);
  }

  @Override
  public FullReportResponse getFullReport() {
    Long totalUsers = dashboardRepository.countTotalUsers();
    Long totalPosts = dashboardRepository.countTotalPosts();
    Long totalComments = (long) commentRepository.count();
    Long totalReactions = (long) postReactionRepository.count();
    Long totalFollowers = (long) userFollowRepository.count();

    Map<String, Long> postStatusSummary = new LinkedHashMap<>();
    postStatusSummary.put("PENDING", dashboardRepository.countPostsByStatus(PostStatus.PENDING));
    postStatusSummary.put("APPROVED", dashboardRepository.countPostsByStatus(PostStatus.APPROVED));
    postStatusSummary.put("REJECTED", dashboardRepository.countPostsByStatus(PostStatus.REJECTED));
    postStatusSummary.put("SOLD", dashboardRepository.countPostsByStatus(PostStatus.SOLD));
    postStatusSummary.put("HIDDEN", dashboardRepository.countPostsByStatus(PostStatus.HIDDEN));
    postStatusSummary.put("VIOLATION", dashboardRepository.countPostsByStatus(PostStatus.VIOLATION));
    postStatusSummary.put("DELETED", 0L);

    Map<String, Long> userStatusSummary = new LinkedHashMap<>();
    userStatusSummary.put("ACTIVE", dashboardRepository.countUsersByStatus(UserStatus.ACTIVE));
    userStatusSummary.put("INACTIVE", dashboardRepository.countUsersByStatus(UserStatus.INACTIVE));
    userStatusSummary.put("BANNED", dashboardRepository.countUsersByStatus(UserStatus.BANNED));
    userStatusSummary.put("DELETED", dashboardRepository.countDeletedUsers());

    return FullReportResponse.builder()
        .postStatusSummary(postStatusSummary)
        .userStatusSummary(userStatusSummary)
        .totalUsers(totalUsers)
        .totalPosts(totalPosts)
        .totalComments(totalComments)
        .totalReactions(totalReactions)
        .totalFollowers(totalFollowers)
        .build();
  }

  @Override
  public Page<UserReportStatsResponse> getUserReportStats(String searchText, int page, int size) {
    List<User> allUsers = userRepository.findAllByRoleName("USER");
    List<User> filtered;

    if (searchText != null && !searchText.trim().isEmpty()) {
      String keyword = searchText.trim().toLowerCase();
      filtered = allUsers.stream()
          .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword))
              || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword)))
          .toList();
    } else {
      filtered = allUsers;
    }

    int total = filtered.size();
    int start = (page - 1) * size;
    int end = Math.min(start + size, total);
    List<User> paged = start < total ? filtered.subList(start, end) : List.of();

    List<UserReportStatsResponse> content = paged.stream().map(user -> {
      Long userTotalPosts = postRepository.countByUserUserId(user.getUserId());
      Long approvedPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.APPROVED);
      Long rejectedPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.REJECTED);
      Long pendingPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.PENDING);
      Long followerCount = userFollowRepository.countFollowersByUserId(user.getUserId());
      Long followingCount = userFollowRepository.countFollowingByUserId(user.getUserId());

      return UserReportStatsResponse.builder()
          .userId(user.getUserId())
          .fullName(user.getFullName())
          .email(user.getEmail())
          .avatarUrl(user.getAvatarUrl())
          .totalPosts(userTotalPosts)
          .approvedPosts(approvedPosts)
          .rejectedPosts(rejectedPosts)
          .pendingPosts(pendingPosts)
          .followerCount(followerCount)
          .followingCount(followingCount)
          .build();
    }).collect(Collectors.toList());

    return new PageImpl<>(content, PageRequest.of(page - 1, size), total);
  }

  @Override
  public List<UserPostDetailResponse> getUserPosts(Long userId) {
    List<Post> posts = postRepository.findByUserUserId(userId);

    Set<Long> postIds = posts.stream().map(Post::getPostId).collect(Collectors.toSet());
    Map<Long, Long> reactionCounts = getReactionCounts(postIds);
    Map<Long, Long> commentCounts = getCommentCounts(postIds);
    Map<Long, Long> viewCounts = getViewCounts(postIds);

    return posts.stream().map(post -> {
      String thumbnail = null;
      if (post.getImages() != null && !post.getImages().isEmpty()) {
        PostImage firstImage = post.getImages().get(0);
        if (firstImage.getObjectKey() != null) {
          thumbnail = buildPublicUrl(firstImage.getObjectKey());
        }
      }

      return UserPostDetailResponse.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .description(post.getDescription())
          .price(post.getPrice())
          .status(post.getStatus() != null ? post.getStatus().toString() : "UNKNOWN")
          .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
          .brandName(post.getBrand() != null ? post.getBrand().getName() : null)
          .conditionGrade(post.getConditionGrade() != null ? post.getConditionGrade().toString() : null)
          .thumbnailUrl(thumbnail)
          .createdAt(post.getCreatedAt())
          .viewCount(viewCounts.getOrDefault(post.getPostId(), 0L))
          .reactionCount(reactionCounts.getOrDefault(post.getPostId(), 0L))
          .commentCount(commentCounts.getOrDefault(post.getPostId(), 0L))
          .build();
    }).toList();
  }

  @Override
  public UserReportStatsResponse getUserStats(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

    Long userTotalPosts = postRepository.countByUserUserId(user.getUserId());
    Long approvedPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.APPROVED);
    Long rejectedPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.REJECTED);
    Long pendingPosts = postRepository.countByUserUserIdAndStatus(user.getUserId(), PostStatus.PENDING);
    Long followerCount = userFollowRepository.countFollowersByUserId(user.getUserId());
    Long followingCount = userFollowRepository.countFollowingByUserId(user.getUserId());

    return UserReportStatsResponse.builder()
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .avatarUrl(user.getAvatarUrl())
        .totalPosts(userTotalPosts)
        .approvedPosts(approvedPosts)
        .rejectedPosts(rejectedPosts)
        .pendingPosts(pendingPosts)
        .followerCount(followerCount)
        .followingCount(followingCount)
        .build();
  }

  private void applyViolationPolicy(User seller) {
    long violationCount30Days = postReportRepository.countViolationPostsByOwnerSince(
        seller.getUserId(), LocalDateTime.now().minusDays(VIOLATION_LOOKBACK_DAYS));

    if (violationCount30Days >= 5) {
      seller.setStatus(UserStatus.BANNED);
      userRepository.save(seller);
      return;
    }

    userRepository.save(seller);
  }

  private void notifyPostOwnerReported(Post post, User reporter, boolean autoHidden, long pendingReporterCount) {
    User owner = post.getUser();
    String title = autoHidden ? "Bài viết bị ẩn tạm thời" : "Bài viết bị báo cáo";
    String content = autoHidden
        ? "Bài viết \"" + post.getTitle() + "\" đã bị ẩn tạm thời vì nhận đủ " + pendingReporterCount
            + " báo cáo từ người dùng khác nhau."
        : "Bài viết \"" + post.getTitle() + "\" vừa nhận thêm một báo cáo và đang chờ quản trị viên xử lý.";
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("postId", post.getPostId());
    data.put("postTitle", post.getTitle());
    data.put("pendingReportCount", pendingReporterCount);
    data.put("autoHidden", autoHidden);

    notificationService.createNotification(
        owner,
        reporter,
        NotificationType.POST_REPORT,
        title,
        content,
        "/post/" + post.getPostId(),
        data);
  }

  private void notifyPostOwnerModerated(Post post, boolean isViolation) {
    User owner = post.getUser();
    String title = isViolation ? "Bài viết vi phạm đã bị xử lý" : "Bài viết được khôi phục";
    String content = isViolation
        ? "Bài viết \"" + post.getTitle() + "\" đã bị xác nhận vi phạm bởi quản trị viên."
        : "Bài viết \"" + post.getTitle() + "\" không vi phạm và đã được hiển thị lại.";
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("postId", post.getPostId());
    data.put("postTitle", post.getTitle());
    data.put("result", isViolation ? "VIOLATION" : "APPROVED");

    notificationService.createNotification(
        owner,
        null,
        NotificationType.POST_REPORT,
        title,
        content,
        "/post/" + post.getPostId(),
        data);
  }

  private void notifyAdminsAboutNewReport(Post post, User reporter) {
    List<User> admins = userRepository.findAllByRoleName("ADMIN");
    String title = "Báo cáo bài viết mới";
    String content = "Bài viết \"" + post.getTitle() + "\" vừa nhận được báo cáo từ "
        + reporter.getFullName() + ". Vui lòng kiểm tra và xử lý.";
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("postId", post.getPostId());
    data.put("postTitle", post.getTitle());
    data.put("reportId", post.getPostId());
    data.put("reporterId", reporter.getUserId());
    data.put("reporterName", reporter.getFullName());

    for (User admin : admins) {
      NotificationResponse notification = notificationService.createNotification(
          admin,
          reporter,
          NotificationType.POST_REPORT,
          title,
          content,
          "/admin/reports",
          data);
      notificationService.sendNotificationToAdminTopic(notification);
    }
  }

  private ReportResponse toReportResponse(PostReport report) {
    ReportResponse.ReporterInfo reporterInfo = ReportResponse.ReporterInfo.builder()
        .userId(report.getReporter().getUserId())
        .fullName(report.getReporter().getFullName())
        .avatarUrl(report.getReporter().getAvatarUrl())
        .build();

    return ReportResponse.builder()
        .reportId(report.getReportId())
        .postId(report.getPost().getPostId())
        .postTitle(report.getPost().getTitle())
        .reporter(reporterInfo)
        .reason(report.getReason())
        .description(report.getDescription())
        .status(report.getStatus().name())
        .createdAt(report.getCreatedAt())
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


  private Map<Long, Long> getReactionCounts(Set<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    return postReactionRepository.countReactionsByPostIds(List.copyOf(postIds)).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private Map<Long, Long> getCommentCounts(Set<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    return commentRepository.countCommentsByPostIds(List.copyOf(postIds)).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private Map<Long, Long> getViewCounts(Set<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    return postViewRepository.countViewsByPostIds(List.copyOf(postIds)).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  @Override
  public List<CategoryStatsResponse> getCategoryStats() {
    List<Category> categories = categoryRepository.findAll();
    long totalPosts = dashboardRepository.countTotalPosts();

    List<CategoryStatsResponse> categoryStats = categories.stream()
        .map(cat -> {
          Long postCount = dashboardRepository.countPostsByCategory(cat.getCategoryId());
          Long approvedCount = dashboardRepository.countApprovedPostsByCategory(cat.getCategoryId());
          Long soldCount = dashboardRepository.countSoldPostsByCategory(cat.getCategoryId());
          Long pendingCount = dashboardRepository.countPendingPostsByCategory(cat.getCategoryId());
          double percentage = totalPosts > 0 ? (postCount * 100.0 / totalPosts) : 0;

          return CategoryStatsResponse.builder()
              .categoryId(cat.getCategoryId())
              .categoryName(cat.getName())
              .postCount(postCount)
              .approvedCount(approvedCount)
              .soldCount(soldCount)
              .pendingCount(pendingCount)
              .percentage(percentage)
              .build();
        })
        .sorted((a, b) -> Long.compare(b.getPostCount(), a.getPostCount()))
        .toList();

    return categoryStats;
  }
}

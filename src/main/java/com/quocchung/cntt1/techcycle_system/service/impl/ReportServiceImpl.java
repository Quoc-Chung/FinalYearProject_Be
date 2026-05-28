package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dto.response.FullReportResponse;
import com.quocchung.cntt1.techcycle_system.dto.response.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dto.response.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.*;
import com.quocchung.cntt1.techcycle_system.service.ReportService;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    // ========== USER REPORT (Post Report) ==========
    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ========== ADMIN STATS ==========
    private final CommentRepository commentRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostViewRepository postViewRepository;
    private final UserFollowRepository userFollowRepository;
    private final DashboardRepository dashboardRepository;
    private final MinioProperties minioProperties;

    // ========== USER REPORT METHODS (Original) ==========

    @Override
    @Transactional
    public ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));

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
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        report = postReportRepository.save(report);

        return toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByPost(Long postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByPostPostIdOrderByCreatedAtDesc(postId, pageable);
        return reports.map(this::toReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByReporterUserId(reporterId, pageable);
        return reports.map(this::toReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return reports.map(this::toReportResponse);
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(Long reportId, Long adminId, ReportStatus status) {
        PostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy báo cáo với id: " + reportId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy admin với id: " + adminId));

        report.setStatus(status);
        report.setResolvedBy(admin);
        report = postReportRepository.save(report);

        return toReportResponse(report);
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

    // ========== ADMIN STATS METHODS (New) ==========

    @Override
    public FullReportResponse getFullReport() {
        Long totalUsers = dashboardRepository.countTotalUsers();
        Long totalPosts = dashboardRepository.countTotalPosts();
        Long totalComments = Long.valueOf(commentRepository.count());
        Long totalReactions = Long.valueOf(postReactionRepository.count());
        Long totalFollowers = Long.valueOf(userFollowRepository.count());

        Map<String, Long> postStatusSummary = new LinkedHashMap<>();
        postStatusSummary.put("PENDING", dashboardRepository.countPostsByStatus(PostStatus.PENDING));
        postStatusSummary.put("APPROVED", dashboardRepository.countPostsByStatus(PostStatus.APPROVED));
        postStatusSummary.put("REJECTED", dashboardRepository.countPostsByStatus(PostStatus.REJECTED));
        postStatusSummary.put("SOLD", dashboardRepository.countPostsByStatus(PostStatus.SOLD));
        postStatusSummary.put("HIDDEN", dashboardRepository.countPostsByStatus(PostStatus.HIDDEN));
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
    public List<UserReportStatsResponse> getUserReportStats(String searchText, int page, int size) {
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

        return paged.stream().map(user -> {
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
        }).collect(Collectors.toList());
    }

    @Override
    public UserReportStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND, "User not found"));

        Long totalPosts = postRepository.countByUserUserId(userId);
        Long approvedPosts = postRepository.countByUserUserIdAndStatus(userId, PostStatus.APPROVED);
        Long rejectedPosts = postRepository.countByUserUserIdAndStatus(userId, PostStatus.REJECTED);
        Long pendingPosts = postRepository.countByUserUserIdAndStatus(userId, PostStatus.PENDING);
        Long followerCount = userFollowRepository.countFollowersByUserId(userId);
        Long followingCount = userFollowRepository.countFollowingByUserId(userId);

        return UserReportStatsResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .totalPosts(totalPosts)
                .approvedPosts(approvedPosts)
                .rejectedPosts(rejectedPosts)
                .pendingPosts(pendingPosts)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    private Map<Long, Long> getReactionCounts(Set<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();
        return postReactionRepository.countReactionsByPostIds(new ArrayList<>(postIds))
                .stream().collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));
    }

    private Map<Long, Long> getCommentCounts(Set<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();
        return commentRepository.countCommentsByPostIds(new ArrayList<>(postIds))
                .stream().collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));
    }

    private Map<Long, Long> getViewCounts(Set<Long> postIds) {
        if (postIds.isEmpty()) return Collections.emptyMap();
        return postViewRepository.countViewsByPostIds(new ArrayList<>(postIds))
                .stream().collect(Collectors.toMap(arr -> (Long) arr[0], arr -> (Long) arr[1]));
    }

    private String buildPublicUrl(String objectKey) {
        String base = minioProperties.getPublicEndpoint();
        if (base == null || base.isBlank()) {
            base = minioProperties.getEndpoint();
        }
        if (base != null && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + minioProperties.getBucketName() + "/" + objectKey;
    }
}

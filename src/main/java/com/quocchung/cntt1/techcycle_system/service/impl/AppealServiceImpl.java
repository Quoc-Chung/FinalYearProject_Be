package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateAppealRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.AppealResponse;
import com.quocchung.cntt1.techcycle_system.model.Appeal;
import com.quocchung.cntt1.techcycle_system.model.Notification;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.AppealRepository;
import com.quocchung.cntt1.techcycle_system.repository.NotificationRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.AppealService;
import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final MinioProperties minioProperties;

    private String buildPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return objectKey;

        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            return objectKey;
        }

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
    @Transactional
    public AppealResponse createAppeal(Long userId, CreateAppealRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (appealRepository.existsByPostPostIdAndUserUserId(request.getPostId(), userId)) {
            throw new RuntimeException("You have already appealed this post");
        }

        Appeal appeal = Appeal.builder()
                .post(post)
                .user(user)
                .reason(request.getReason())
                .status(AppealStatus.PENDING)
                .build();

        appeal = appealRepository.save(appeal);

     
        notifyAdminsAboutNewAppeal(appeal);

        return mapToResponse(appeal);
    }

    @Override
    public AppealResponse getAppealById(Long appealId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));
        return mapToResponse(appeal);
    }

    @Override
    public Page<AppealResponse> getAppealsByStatus(AppealStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return appealRepository.findByStatus(status, pageRequest).map(this::mapToResponse);
    }

    @Override
    public Page<AppealResponse> getAllAppeals(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return appealRepository.findAll(pageRequest).map(this::mapToResponse);
    }

    @Override
    public Page<AppealResponse> getAppealsByUser(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return appealRepository.findByUserId(userId, pageRequest).map(this::mapToResponse);
    }

    @Override
    public boolean hasUserAppealedPost(Long userId, Long postId) {
        return appealRepository.existsByPostPostIdAndUserUserId(postId, userId);
    }

    @Override
    @Transactional
    public AppealResponse processAppeal(Long appealId, Long adminId, AppealStatus decision, String adminNote) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new RuntimeException("Appeal has already been processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        appeal.setStatus(decision);
        appeal.setProcessedBy(admin);
        appeal.setProcessedAt(LocalDateTime.now());
        appeal.setAdminNote(adminNote);

        Post post = appeal.getPost();

        if (decision == AppealStatus.APPROVED) {
            // Post is not violation - approve it
            post.setStatus(PostStatus.APPROVED);
            post.setApprovedBy(admin.getUserId());
            post.setApprovedAt(LocalDateTime.now());
            postRepository.save(post);

            // Notify user: Appeal approved
            sendNotification(
                    appeal.getUser(),
                    NotificationType.APPEAL_APPROVED,
                    "Khiếu nại được chấp nhận",
                    "Khiếu nại của bạn đã được chấp nhận. Bài viết \"" + post.getTitle() + "\" đã được khôi phục và hiển thị trở lại.",
                    "/my-posts",
                    Map.of("postId", post.getPostId(), "status", "APPROVED")
            );
        } else {

            sendNotification(
                    appeal.getUser(),
                    NotificationType.APPEAL_REJECTED,
                    "Khiếu nại bị từ chối",
                    "Khiếu nại của bạn đã bị từ chối. Bài viết \"" + post.getTitle() + "\" vẫn bị đánh dấu vi phạm.",
                    "/my-posts",
                    Map.of("postId", post.getPostId(), "status", "VIOLATION")
            );
        }

        postRepository.save(post);
        appeal = appealRepository.save(appeal);

        return mapToResponse(appeal);
    }

    @Override
    public long countPendingAppeals() {
        return appealRepository.countByStatus(AppealStatus.PENDING);
    }

    private AppealResponse mapToResponse(Appeal appeal) {
        AppealResponse.UserInfo userInfo = AppealResponse.UserInfo.builder()
                .userId(appeal.getUser().getUserId())
                .fullName(appeal.getUser().getFullName())
                .avatarUrl(appeal.getUser().getAvatarUrl())
                .build();

        AppealResponse.AdminInfo adminInfo = null;
        if (appeal.getProcessedBy() != null) {
            adminInfo = AppealResponse.AdminInfo.builder()
                    .userId(appeal.getProcessedBy().getUserId())
                    .fullName(appeal.getProcessedBy().getFullName())
                    .build();
        }

        String thumbnail = null;
        if (appeal.getPost().getImages() != null && !appeal.getPost().getImages().isEmpty()) {
            PostImage firstImage = appeal.getPost().getImages().get(0);
            if (firstImage.getObjectKey() != null) {
                thumbnail = buildPublicUrl(firstImage.getObjectKey());
            }
        }

        return AppealResponse.builder()
                .appealId(appeal.getAppealId())
                .postId(appeal.getPost().getPostId())
                .postTitle(appeal.getPost().getTitle())
                .postThumbnail(thumbnail)
                .user(userInfo)
                .reason(appeal.getReason())
                .adminNote(appeal.getAdminNote())
                .status(appeal.getStatus())
                .processedBy(adminInfo)
                .processedAt(appeal.getProcessedAt())
                .createdAt(appeal.getCreatedAt())
                .build();
    }

    private void notifyAdminsAboutNewAppeal(Appeal appeal) {
        // Get all admins
        userRepository.getAllAdmin().forEach(admin -> {
            sendNotification(
                    admin,
                    NotificationType.APPEAL_SUBMITTED,
                    "Yêu cầu khiếu nại mới",
                    "Người dùng " + appeal.getUser().getFullName() + " đã gửi khiếu nại cho bài viết \"" + appeal.getPost().getTitle() + "\"",
                    "/admin/appeals",
                    Map.of(
                            "appealId", appeal.getAppealId(),
                            "postId", appeal.getPost().getPostId(),
                            "userId", appeal.getUser().getUserId()
                    )
            );
        });
    }

    private void sendNotification(User user, NotificationType type, String title, String content, String targetUrl, Map<String, Object> data) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .targetUrl(targetUrl)
                .data(new HashMap<>(data))
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}

package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Review.PostReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.PostReviewResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostReview;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReviewRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.service.PostReviewService;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostReviewServiceImpl implements PostReviewService {

    private final PostReviewRepository postReviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    @Override
    @Transactional
    public PostReviewResponse createReview(Long postId, Long userId, PostReviewRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng với id: " + userId));
        if (postReviewRepository.existsByPostPostIdAndUserUserId(postId, userId)) {
            throw new ResException(ResErrorCode.INVALID_REQUEST,
                    "Bạn đã đánh giá bài đăng này rồi");
        }
        PostReview review = PostReview.builder()
                .post(post)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = postReviewRepository.save(review);

       
        User postOwner = post.getUser();
        if (postOwner != null && !postOwner.getUserId().equals(userId)) {
            String title = "Bài viết được đánh giá mới";
            String content = user.getFullName() + " đã đánh giá " + request.getRating() + " sao cho bài viết \"" + post.getTitle() + "\"";
            String targetUrl = "/post/" + postId;
            Map<String, Object> data = new HashMap<>();
            data.put("postId", postId);
            data.put("postTitle", post.getTitle());
            data.put("rating", request.getRating());
            data.put("reviewId", review.getReviewId());

            notificationService.createNotification(
                    postOwner,  // recipient - chủ bài viết
                    user,       // actor - người đánh giá
                    NotificationType.POST_REVIEW,
                    title,
                    content,
                    targetUrl,
                    data
            );
        }

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostReviewResponse> getReviewsByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReview> reviews = postReviewRepository.findByPostPostIdOrderByCreatedAtDesc(postId, pageable);
        return reviews.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostReviewResponse> getReviewsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReview> reviews = postReviewRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviews.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PostReviewResponse getReviewStats(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));
        Double avgRating = postReviewRepository.getAverageRatingByPostId(postId);
        long count = postReviewRepository.countByPostId(postId);
        return PostReviewResponse.builder()
                .postId(postId)
                .rating(avgRating != null ? avgRating.intValue() : 0)
                .comment(count + " đánh giá")
                .build();
    }

    private PostReviewResponse toResponse(PostReview review) {
        PostReviewResponse.UserSummaryResponse userSummary = PostReviewResponse.UserSummaryResponse.builder()
                .userId(review.getUser().getUserId())
                .fullName(review.getUser().getFullName())
                .avatarUrl(review.getUser().getAvatarUrl())
                .build();
        return PostReviewResponse.builder()
                .reviewId(review.getReviewId())
                .postId(review.getPost().getPostId())
                .postTitle(review.getPost().getTitle())
                .user(userSummary)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

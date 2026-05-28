package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Review.PostReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.PostReviewResponse;
import org.springframework.data.domain.Page;

public interface PostReviewService {

    PostReviewResponse createReview(Long postId, Long userId, PostReviewRequest request);

    Page<PostReviewResponse> getReviewsByPostId(Long postId, int page, int size);

    Page<PostReviewResponse> getReviewsByUserId(Long userId, int page, int size);

    PostReviewResponse getReviewStats(Long postId);
}

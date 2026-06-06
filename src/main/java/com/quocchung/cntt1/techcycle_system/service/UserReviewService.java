package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Review.ReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.ReviewResponse;
import org.springframework.data.domain.Page;

public interface UserReviewService {
  ReviewResponse reviewUser(Long fromUserId, Long toUserId, ReviewRequest request);

  Page<ReviewResponse> getReviews(Long userId, int page, int size);

  boolean hasTransactionWithUser(Long userId1, Long userId2);
}

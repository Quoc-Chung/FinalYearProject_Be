package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Review.ReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.ReviewResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.UserSummaryResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserReview;
import com.quocchung.cntt1.techcycle_system.repository.TransactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserReviewRepository;
import com.quocchung.cntt1.techcycle_system.service.UserReviewService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReviewServiceImpl implements UserReviewService {
  private final UserReviewRepository userReviewRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  @Override
  @Transactional
  public ReviewResponse reviewUser(Long fromUserId, Long toUserId, ReviewRequest request) {
    if (fromUserId.equals(toUserId)) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "Không thể tự đánh giá bản thân");
    }
    if (!transactionRepository.existsCompletedTransactionBetweenUsers(fromUserId, toUserId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED,
          "Chỉ có thể đánh giá người dùng khi đã có giao dịch hoàn tất với họ");
    }

    User fromUser = getUserOrThrow(fromUserId);
    User toUser = getUserOrThrow(toUserId);
    var transactionOpt = transactionRepository.findCompletedTransactionBetweenUsers(fromUserId, toUserId);

    Optional<UserReview> existingReview = userReviewRepository.findByFromUserAndToUser(fromUser, toUser);

    UserReview review;

    if (existingReview.isPresent()) {
      review = existingReview.get();
      review.setComment(request.getComment());
      review.setTags(request.getTags());
      if (review.getTransaction() == null && transactionOpt.isPresent()) {
        review.setTransaction(transactionOpt.get());
      }

    } else {
      review = UserReview.builder()
          .fromUser(fromUser)
          .toUser(toUser)
          .rating(request.getRating())
          .comment(request.getComment())
          .tags(request.getTags())
          .transaction(transactionOpt.orElse(null))
          .build();
    }
    userReviewRepository.save(review);
    recalculateTrustScore(toUser);
    return ReviewResponse.builder()
        .reviewId(review.getReviewId())
        .fromUser(toUserSummary(fromUser))
        .rating(review.getRating())
        .comment(review.getComment())
        .tags(review.getTags())
        .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
        .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null)
        .build();
  }

  @Override
  public Page<ReviewResponse> getReviews(Long userId, int page, int size) {
    getUserOrThrow(userId);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    Page<UserReview> reviews = userReviewRepository.findByToUserId(userId, pageable);

    return reviews.map(r -> ReviewResponse.builder()
        .reviewId(r.getReviewId())
        .fromUser(toUserSummary(r.getFromUser()))
        .rating(r.getRating())
        .comment(r.getComment())
        .tags(r.getTags())
        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
        .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
        .build());
  }

  private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));
  }


  private void recalculateTrustScore(User user) {
    Double avgRating = userReviewRepository.getAverageRatingByUserId(user.getUserId());
    long totalReviews = userReviewRepository.countByToUserId(user.getUserId());

    if (avgRating == null || totalReviews == 0) {
      user.setTrustScore(0.0);
      userRepository.save(user);
      return;
    }
    double C = 3.5;
    double m = 10.0;
    double bayesianAvg = (m * C + avgRating * totalReviews) / (m + totalReviews);
    double score = (bayesianAvg / 5.0) * 100.0;
    user.setTrustScore(Math.round(score * 10.0) / 10.0);
    userRepository.save(user);
  }

  private UserSummaryResponse toUserSummary(User user) {
    return UserSummaryResponse.builder()
        .userId(user.getUserId())
        .fullName(user.getFullName())
        .avatarUrl(user.getAvatarUrl())
        .trustScore(user.getTrustScore())
        .build();
  }

  @Override
  public boolean hasTransactionWithUser(Long userId1, Long userId2) {
    return transactionRepository.existsCompletedTransactionBetweenUsers(userId1, userId2);
  }
}

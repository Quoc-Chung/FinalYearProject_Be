package com.quocchung.cntt1.techcycle_system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocchung.cntt1.techcycle_system.dtos.request.Review.ReviewRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.ReviewResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Review.UserSummaryResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Transaction;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserReview;
import com.quocchung.cntt1.techcycle_system.repository.TransactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserReviewRepository;
import com.quocchung.cntt1.techcycle_system.service.UserReviewService;
import java.util.List;
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
  private final ObjectMapper objectMapper;

  private String tagsToJson(List<String> tags) {
    if (tags == null || tags.isEmpty()) return "[]";
    try {
      return objectMapper.writeValueAsString(tags);
    } catch (JsonProcessingException e) {
      return "[]";
    }
  }

  private List<String> jsonToTags(String json) {
    if (json == null || json.isEmpty()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (JsonProcessingException e) {
      return List.of();
    }
  }
  @Override
  @Transactional
  public ReviewResponse reviewUser(Long fromUserId, Long toUserId, ReviewRequest request) {
    if (fromUserId.equals(toUserId)) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "Không thể tự đánh giá bản thân");
    }

    User fromUser = getUserOrThrow(fromUserId);
    User toUser = getUserOrThrow(toUserId);

    Transaction transaction = null;
    if (request.getTransactionId() != null) {
      transaction = transactionRepository.findById(request.getTransactionId())
          .orElseThrow(() -> new ResException(ResErrorCode.BAD_REQUEST,
              "Không tìm thấy giao dịch với id: " + request.getTransactionId()));

      boolean isValidTransaction = (transaction.getSeller().getUserId().equals(fromUserId)
          && transaction.getBuyer().getUserId().equals(toUserId))
          || (transaction.getSeller().getUserId().equals(toUserId)
          && transaction.getBuyer().getUserId().equals(fromUserId));

      if (!isValidTransaction) {
        throw new ResException(ResErrorCode.PERMISSION_DENIED,
            "Giao dịch không hợp lệ giữa hai người dùng");
      }

      boolean isSeller = transaction.getSeller().getUserId().equals(fromUserId);
      transaction.setSellerReviewed(isSeller ? true : transaction.getSellerReviewed());
      transaction.setBuyerReviewed(isSeller ? transaction.getBuyerReviewed() : true);
      transactionRepository.save(transaction);
    }

    Optional<UserReview> existingReview = userReviewRepository.findByFromUserAndToUser(fromUser, toUser);

    UserReview review;

    if (existingReview.isPresent()) {
      review = existingReview.get();
      review.setComment(request.getComment());
      review.setTags(tagsToJson(request.getTags()));
      if (transaction != null) {
        review.setTransaction(transaction);
      }
    } else {
      review = UserReview.builder()
          .fromUser(fromUser)
          .toUser(toUser)
          .rating(request.getRating())
          .comment(request.getComment())
          .tags(tagsToJson(request.getTags()))
          .transaction(transaction)
          .build();
    }

    userReviewRepository.save(review);
    recalculateTrustScore(toUser);

    return ReviewResponse.builder()
        .reviewId(review.getReviewId())
        .fromUser(toUserSummary(fromUser))
        .rating(review.getRating())
        .comment(review.getComment())
        .tags(jsonToTags(review.getTags()))
        .transactionId(review.getTransaction() != null ? review.getTransaction().getTransactionId() : null)
        .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
        .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null)
        .build();
  }

  @Override
  public Page<ReviewResponse> getReviews(Long userId, int page, int size) {
    getUserOrThrow(userId);

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); 
    Page<UserReview> reviews = userReviewRepository.findByToUserId(userId, pageable);

    return reviews.map(r -> ReviewResponse.builder()
        .reviewId(r.getReviewId())
        .fromUser(toUserSummary(r.getFromUser()))
        .rating(r.getRating())
        .comment(r.getComment())
        .tags(jsonToTags(r.getTags()))
        .transactionId(r.getTransaction() != null ? r.getTransaction().getTransactionId() : null)
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

package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.Transaction.MarkSoldRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Transation.TransactionResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.Transaction;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.TransactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.service.TransactionService;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.TransactionStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private static final long REVIEW_WINDOW_DAYS = 30L;

  private final TransactionRepository transactionRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final MinioProperties minioProperties;

  @Override
  @Transactional
  public TransactionResponse markAsSold(Long sellerId, MarkSoldRequest request) {
    Post post = postRepository.findById(request.getPostId())
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND, "Không tìm thấy bài"));

    if (!post.getUser().getUserId().equals(sellerId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED, "Bạn không phải chủ bài viết này");
    }

    if (post.getStatus() != PostStatus.APPROVED) {
      throw new ResException(ResErrorCode.INVALID_REQUEST,
          "Chỉ có thể đánh dấu đã bán với bài viết đang ở trạng thái APPROVED");
    }

    if (request.getBuyerId().equals(sellerId)) {
      throw new ResException(ResErrorCode.INVALID_REQUEST, "Không thể tự mua bài của chính mình");
    }

    if (transactionRepository.existsByPostPostId(request.getPostId())) {
      throw new ResException(ResErrorCode.INVALID_REQUEST, "Bài viết này đã được đánh dấu bán rồi");
    }

    User buyer = userRepository.findById(request.getBuyerId())
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND, "Không tìm thấy người mua"));

    User seller = post.getUser();
    Transaction transaction = Transaction.builder()
        .post(post)
        .seller(seller)
        .buyer(buyer)
        .status(TransactionStatus.COMPLETED)
        .sellerReviewed(false)
        .buyerReviewed(false)
        .build();

    transaction = transactionRepository.save(transaction);
    post.setStatus(PostStatus.SOLD);
    postRepository.save(post);

    notifyTransactionCompleted(transaction);
    return toTransactionResponse(transaction, sellerId);
  }

  @Override
  public Page<TransactionResponse> getMyTransactions(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Transaction> transactions = transactionRepository
        .findBySellerUserIdOrBuyerUserIdOrderByCreatedAtDesc(userId, userId, pageable);
    return transactions.map(tx -> toTransactionResponse(tx, userId));
  }

  @Override
  @Transactional
  public void cancelTransaction(Long transactionId) {
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Không tìm thấy giao dịch"));

    Post post = transaction.getPost();
    post.setStatus(PostStatus.APPROVED);
    postRepository.save(post);
  }

  private void notifyTransactionCompleted(Transaction transaction) {
    Map<String, Object> sellerData = new LinkedHashMap<>();
    sellerData.put("transactionId", transaction.getTransactionId());
    sellerData.put("postId", transaction.getPost().getPostId());
    sellerData.put("postTitle", transaction.getPost().getTitle());
    sellerData.put("role", "SELLER");
    notificationService.createNotification(
        transaction.getSeller(),
        transaction.getBuyer(),
        NotificationType.SYSTEM,
        "Giao dịch hoàn tất",
        "Giao dịch cho bài viết \"" + transaction.getPost().getTitle()
            + "\" đã hoàn tất. Bạn có 30 ngày để đánh giá đối tác.",
        "/transactions",
        sellerData);

    Map<String, Object> buyerData = new LinkedHashMap<>();
    buyerData.put("transactionId", transaction.getTransactionId());
    buyerData.put("postId", transaction.getPost().getPostId());
    buyerData.put("postTitle", transaction.getPost().getTitle());
    buyerData.put("role", "BUYER");
    notificationService.createNotification(
        transaction.getBuyer(),
        transaction.getSeller(),
        NotificationType.SYSTEM,
        "Giao dịch hoàn tất",
        "Giao dịch cho bài viết \"" + transaction.getPost().getTitle()
            + "\" đã hoàn tất. Bạn có 30 ngày để đánh giá đối tác.",
        "/transactions",
        buyerData);
  }

  private String buildPublicUrl(String objectKey) {
    String base = minioProperties.getPublicEndpoint();
    if (base == null || base.isBlank()) {
      base = minioProperties.getEndpoint();
    }
    return base + "/" + minioProperties.getBucketName() + "/" + objectKey;
  }

  private TransactionResponse toTransactionResponse(Transaction tx, Long currentUserId) {
    long daysSince = ChronoUnit.DAYS.between(tx.getCreatedAt(), LocalDateTime.now());
    boolean withinReviewWindow = daysSince <= REVIEW_WINDOW_DAYS;
    boolean isSeller = tx.getSeller().getUserId().equals(currentUserId);

    boolean canReview = withinReviewWindow
        && tx.getStatus() == TransactionStatus.COMPLETED
        && (isSeller ? !Boolean.TRUE.equals(tx.getSellerReviewed())
            : !Boolean.TRUE.equals(tx.getBuyerReviewed()));

    String thumbnail = null;
    if (tx.getPost().getImages() != null && !tx.getPost().getImages().isEmpty()) {
      String objectKey = tx.getPost().getImages().get(0).getObjectKey();
      if (objectKey != null && !objectKey.isBlank()) {
        thumbnail = buildPublicUrl(objectKey);
      }
    }

    return TransactionResponse.builder()
        .transactionId(tx.getTransactionId())
        .postId(tx.getPost().getPostId())
        .postTitle(tx.getPost().getTitle())
        .thumbnailUrl(thumbnail)
        .price(tx.getPost().getPrice())
        .sellerId(tx.getSeller().getUserId())
        .sellerName(tx.getSeller().getFullName())
        .buyerId(tx.getBuyer().getUserId())
        .buyerName(tx.getBuyer().getFullName())
        .status(tx.getStatus().name())
        .sellerReviewed(tx.getSellerReviewed())
        .buyerReviewed(tx.getBuyerReviewed())
        .canReview(canReview)
        .createdAt(tx.getCreatedAt())
        .build();
  }
}

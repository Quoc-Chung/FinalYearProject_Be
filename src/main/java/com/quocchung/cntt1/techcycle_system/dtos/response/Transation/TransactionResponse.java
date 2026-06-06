package com.quocchung.cntt1.techcycle_system.dtos.response.Transation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
  private Long transactionId;
  private Long postId;
  private String postTitle;
  private String thumbnailUrl;
  private BigDecimal price;

  private Long sellerId;
  private String sellerName;

  private Long buyerId;
  private String buyerName;

  private String status;
  private Boolean sellerReviewed;
  private Boolean buyerReviewed;
  private Boolean canReview;     // Còn trong 30 ngày không
  private LocalDateTime createdAt;

}
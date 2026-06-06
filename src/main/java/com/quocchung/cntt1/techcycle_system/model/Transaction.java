package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.TransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transactions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transactionId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @ManyToOne
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private TransactionStatus status = TransactionStatus.COMPLETED;

  private Boolean sellerReviewed = false;

  private Boolean buyerReviewed = false;

  @CreationTimestamp
  private LocalDateTime createdAt;
}

package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "appeals",
    indexes = {
      @Index(name = "idx_appeal_post", columnList = "post_id"),
      @Index(name = "idx_appeal_user", columnList = "user_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appeal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "appeal_id", updatable = false, nullable = false)
  private Long appealId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
  private String reason;

  @Column(name = "admin_note", columnDefinition = "TEXT")
  private String adminNote;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AppealStatus status = AppealStatus.PENDING;

  @ManyToOne
  @JoinColumn(name = "processed_by")
  private User processedBy;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}

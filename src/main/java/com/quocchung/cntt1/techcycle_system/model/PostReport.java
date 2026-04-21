package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "post_reports",
    indexes = {
      @Index(name = "idx_post_report_post", columnList = "post_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_id", updatable = false, nullable = false)
  private Long reportId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporter;

  @Column(name = "reason", nullable = false, length = 255)
  private String reason;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReportStatus status = ReportStatus.PENDING;

  @ManyToOne
  @JoinColumn(name = "resolved_by")
  private User resolvedBy;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

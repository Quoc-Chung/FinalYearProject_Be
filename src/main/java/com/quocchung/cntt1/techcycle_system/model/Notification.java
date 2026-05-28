package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_notification_user", columnList = "user_id"),
      @Index(name = "idx_notification_is_read", columnList = "is_read"),
      @Index(name = "idx_notification_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  // Người nhận email
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "actor_id")
  private User actor;

  // Loại thong bao
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType type;

  // Tieu de
  private String title;

  // Noi dung
  @Column(columnDefinition = "TEXT")
  private String content;

  // duong dan khi nhan vao no se chuyen tiep den
  @Column(name = "target_url")
  private String targetUrl;

  // data kem thong bao
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Map<String, Object> data;

  // da doc thong bao chua
  @Column(nullable = false)
  private Boolean isRead = false;

  @CreationTimestamp
  private LocalDateTime createdAt;
}

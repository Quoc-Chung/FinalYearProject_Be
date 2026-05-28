package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "notification_preferences",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notification_pref_user_type",
            columnNames = {"user_id", "notification_type"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pref_id", updatable = false, nullable = false)
  private Long prefId;

  // Người sở hữu cấu hình
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Loại notification
  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 100)
  private NotificationType notificationType;

  // Bật/tắt thông báo trong web app
  @Builder.Default
  @Column(name = "in_app", nullable = false)
  private Boolean inApp = true;
}

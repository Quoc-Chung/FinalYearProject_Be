package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "notification_preferences",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_notification_pref_user_type", columnNames = {"user_id", "notification_type"})
    })
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

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false, length = 100)
  private NotificationType notificationType;

  @Builder.Default
  @Column(name = "in_app", nullable = false)
  private Boolean inApp = true;

  @Builder.Default
  @Column(name = "email", nullable = false)
  private Boolean email = true;

  @Builder.Default
  @Column(name = "push", nullable = false)
  private Boolean push = true;
}

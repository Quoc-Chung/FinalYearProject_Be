package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_preferences",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_preferences_user",
            columnNames = {"user_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pref_id", updatable = false, nullable = false)
  private Long prefId;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Province preference (tên tỉnh/thành phố)
  @Column(name = "preferred_province", length = 255)
  private String preferredProvince;

  // Interest types: buy, sell, rating, follow (comma separated)
  @Column(name = "interests", length = 500)
  private String interests;

  // Notification preferences (JSON string)
  @Column(name = "notification_settings", columnDefinition = "TEXT")
  private String notificationSettings;

  // Welcome dialog completed
  @Builder.Default
  @Column(name = "welcome_completed", nullable = false)
  private Boolean welcomeCompleted = false;
}

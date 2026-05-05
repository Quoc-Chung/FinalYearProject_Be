package com.quocchung.cntt1.techcycle_system.model;


import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", updatable = false, nullable = false)
  private Long userId;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(length = 20)
  private String phone;

  @Column(name="bio")
  private String bio;

  @Column(name = "avatar_url", length = 5000)
  private String avatarUrl;


  @Column(name = "is_first_login", nullable = false)
  private Boolean isFirstLogin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "trust_score")
  @Builder.Default
  private Integer trustScore = 0;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @CreationTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  @UpdateTimestamp
  private LocalDateTime deletedAt;

}
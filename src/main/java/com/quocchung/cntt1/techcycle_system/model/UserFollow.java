package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "user_follows",
    indexes = {
      @Index(name = "idx_user_follow_follower", columnList = "follower_id"),
      @Index(name = "idx_user_follow_following", columnList = "following_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_follow_follower_following",
          columnNames = {"follower_id", "following_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "follow_id", updatable = false, nullable = false)
  private Long followId;

  @ManyToOne
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne
  @JoinColumn(name = "following_id", nullable = false)
  private User following;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

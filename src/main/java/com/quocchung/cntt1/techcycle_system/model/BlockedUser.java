package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "blocked_users",
    indexes = {
      @Index(name = "idx_blocked_user_blocker", columnList = "blocker_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_blocked_user_blocker_blocked", columnNames = {"blocker_id", "blocked_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "block_id", updatable = false, nullable = false)
  private Long blockId;

  @ManyToOne
  @JoinColumn(name = "blocker_id", nullable = false)
  private User blocker;

  @ManyToOne
  @JoinColumn(name = "blocked_id", nullable = false)
  private User blocked;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

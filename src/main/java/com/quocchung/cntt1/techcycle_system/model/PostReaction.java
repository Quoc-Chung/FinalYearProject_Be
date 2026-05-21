package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "post_reactions",
    indexes = {
      @Index(name = "idx_post_reaction_post", columnList = "post_id"),
      @Index(name = "idx_post_reaction_user", columnList = "user_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_post_reaction_post_user", columnNames = {"post_id", "user_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reaction_id", updatable = false, nullable = false)
  private Long reactionId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "reaction_type", nullable = false, length = 20)
  private ReactionType reactionType;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

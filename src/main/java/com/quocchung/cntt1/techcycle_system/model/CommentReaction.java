package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "comment_reactions",
    indexes = {
        @Index(name = "idx_comment_reaction_comment", columnList = "comment_id"),
        @Index(name = "idx_comment_reaction_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_comment_reaction_comment_user",
            columnNames = {"comment_id", "user_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reaction_id", updatable = false, nullable = false)
  private Long reactionId;

  @ManyToOne
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;

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
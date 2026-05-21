package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "comments",
    indexes = {
      @Index(name = "idx_comment_post", columnList = "post_id"),
      @Index(name = "idx_comment_user", columnList = "user_id"),
      @Index(name = "idx_comment_parent", columnList = "parent_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id", updatable = false, nullable = false)
  private Long commentId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder.Default
  @Column(name = "likes_count", nullable = false)
  private Integer likesCount = 0;

  @Builder.Default
  @Column(name = "is_hidden", nullable = false)
  private Boolean isHidden = false;

  @Builder.Default
  @Column(name = "is_pinned", nullable = false)
  private Boolean isPinned = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}

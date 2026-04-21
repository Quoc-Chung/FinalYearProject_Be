package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "saved_posts",
    indexes = {
      @Index(name = "idx_saved_post_post", columnList = "post_id"),
      @Index(name = "idx_saved_post_collection", columnList = "collection_id"),
      @Index(name = "idx_saved_post_user", columnList = "user_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "saved_id", updatable = false, nullable = false)
  private Long savedId;

  @ManyToOne
  @JoinColumn(name = "collection_id", nullable = false)
  private PostCollection collection;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Builder.Default
  @Column(name = "notify_on_update", nullable = false)
  private Boolean notifyOnUpdate = false;

  @CreationTimestamp
  @Column(name = "saved_at", updatable = false)
  private LocalDateTime savedAt;
}

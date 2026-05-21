package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "collections",
    indexes = {
      @Index(name = "idx_collection_user", columnList = "user_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCollection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "collection_id", updatable = false, nullable = false)
  private Long collectionId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Builder.Default
  @Column(name = "is_default", nullable = false)
  private Boolean isDefault = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

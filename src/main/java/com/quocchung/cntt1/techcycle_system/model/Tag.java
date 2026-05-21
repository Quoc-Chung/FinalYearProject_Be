package com.quocchung.cntt1.techcycle_system.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tags",
    indexes = @Index(name = "idx_tag_slug", columnList = "slug"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tag_id", updatable = false, nullable = false)
  private Long tagId;

  @Column(nullable = false, length = 100)
  private String name; // "iPhone", "14 Pro Max"

  @Column(nullable = false, unique = true, length = 100)
  private String slug; // "iphone", "14-pro-max"

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;
}
package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.ConditionGrade;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "posts",
    indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_category", columnList = "category_id"),
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id", updatable = false, nullable = false)
  private Long postId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne
  @JoinColumn(name = "brand_id")
  private Brand brand;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private BigDecimal price;

  private BigDecimal originalPrice;

  @Enumerated(EnumType.STRING)
  private ConditionGrade conditionGrade;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PostStatus status = PostStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_id")
  private Address address;

  @Column(name = "approved_by")
  private Long approvedBy;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name= "rejected_reason")
  private String rejectedReason;

  @Column(name="created_at")
  private LocalDateTime createdAt;

  @Column(name="updated_at")
  private LocalDateTime updatedAt;

  @Column(name="delete_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostImage> images = new java.util.ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostAttribute> attributes = new java.util.ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostTag> postTags = new java.util.ArrayList<>();
}
package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.*;

@Entity
@Table(name = "post_views",
    indexes = {
        @Index(name = "idx_post", columnList = "post_id"),
        @Index(name = "idx_viewed_at", columnList = "viewed_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostView {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long viewId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  private Long userId;

  private String ipAddress;

  private LocalDateTime viewedAt;

}
package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_statistics",
    indexes = {
        @Index(name = "idx_post", columnList = "post_id"),
        @Index(name = "idx_date", columnList = "stat_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostStatistic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long statId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  private LocalDate statDate;

  // Tổng số lượt xem ngày
  @Builder.Default
  private Integer viewCount = 0;

  // Tổng tương tác
  @Builder.Default
  private Integer interactionCount = 0;

  // Số người bấm lưu bài
  @Builder.Default
  private Integer saveCount = 0;

  // Số lần chia sẻ
  @Builder.Default
  private Integer shareCount = 0;

  // Số tin nhắn
  @Builder.Default
  private Integer messageCount = 0;
}
package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "comment_images",
    indexes = {
      @Index(name = "idx_comment_image_comment", columnList = "comment_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id", updatable = false, nullable = false)
  private Long imageId;

  @ManyToOne
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;

  private String bucket;

  @Column(name = "object_key", unique = true, nullable = false)
  private String objectKey;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "mime_type")
  private String mimeType;

  @Column(name = "file_size")
  private Long fileSize;

  private Integer width;

  private Integer height;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

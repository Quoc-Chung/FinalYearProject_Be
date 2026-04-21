package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


@Entity
@Table(name = "post_images",
    indexes = @Index(name = "idx_post", columnList = "post_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long imageId;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Enumerated(EnumType.STRING)
  private MediaType mediaType; // IMAGE / VIDEO


  private String bucket;

  // đường dẫn file
  @Column(name = "object_key", unique = true)
  private String objectKey;

  private String fileName;

  // loại file
  private String mimeType;

  private Long fileSize;

  private Integer width;
  private Integer height;

  // chỉ video
  private Double duration;
  private String thumbnailKey;

  private Integer sortOrder;

  @Column(name="created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

}
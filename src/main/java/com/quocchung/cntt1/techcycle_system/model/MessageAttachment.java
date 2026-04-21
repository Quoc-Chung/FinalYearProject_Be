package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "message_attachments",
    indexes = {
      @Index(name = "idx_message_attachment_message", columnList = "message_id"),
      @Index(name = "idx_message_attachment_media_type", columnList = "media_type")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attachment_id", updatable = false, nullable = false)
  private Long attachmentId;

  @ManyToOne
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  @Enumerated(EnumType.STRING)
  @Column(name = "media_type", nullable = false, length = 20)
  private MediaType mediaType;

  @Column(name = "object_key", nullable = false, length = 500)
  private String objectKey;

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}

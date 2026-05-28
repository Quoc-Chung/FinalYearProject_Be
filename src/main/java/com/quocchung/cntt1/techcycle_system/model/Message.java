package com.quocchung.cntt1.techcycle_system.model;

import com.quocchung.cntt1.techcycle_system.utils.enums.MessageType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "messages",
    indexes = {
      @Index(name = "idx_message_conversation", columnList = "conversation_id"),
      @Index(name = "idx_message_created_at", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id", updatable = false, nullable = false)
  private Long messageId;

  @ManyToOne
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false, length = 20)
  private MessageType messageType;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  // Keep FK as scalar because price offer entity is not yet implemented.
  @Column(name = "offer_id")
  private Long offerId;

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

}

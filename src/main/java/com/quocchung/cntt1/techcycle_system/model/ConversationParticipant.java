package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "conversation_participants",
    indexes = {
      @Index(name = "idx_conversation_participant_conversation", columnList = "conversation_id"),
      @Index(name = "idx_conversation_participant_user", columnList = "user_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_conversation_participant_conversation_user",
          columnNames = {"conversation_id", "user_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "participant_id", updatable = false, nullable = false)
  private Long participantId;

  @ManyToOne
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Builder.Default
  @Column(name = "is_blocked", nullable = false)
  private Boolean isBlocked = false;

  @Column(name = "last_read_at")
  private LocalDateTime lastReadAt;

  @CreationTimestamp
  @Column(name = "joined_at", updatable = false)
  private LocalDateTime joinedAt;

}

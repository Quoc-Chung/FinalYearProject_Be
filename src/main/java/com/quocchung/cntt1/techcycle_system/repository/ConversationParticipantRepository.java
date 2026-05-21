package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

  @Query("SELECT cp FROM ConversationParticipant cp " +
         "JOIN FETCH cp.user " +
         "WHERE cp.conversation.conversationId = :conversationId")
  List<ConversationParticipant> findByConversationIdWithUser(@Param("conversationId") Long conversationId);

  @Query("SELECT cp FROM ConversationParticipant cp " +
         "WHERE cp.conversation.conversationId = :conversationId " +
         "AND cp.user.userId = :userId")
  Optional<ConversationParticipant> findByConversationIdAndUserId(
      @Param("conversationId") Long conversationId,
      @Param("userId") Long userId
  );

  @Query("SELECT cp FROM ConversationParticipant cp " +
         "WHERE cp.user.userId = :userId")
  List<ConversationParticipant> findByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("UPDATE ConversationParticipant cp " +
         "SET cp.lastReadAt = :lastReadAt " +
         "WHERE cp.conversation.conversationId = :conversationId " +
         "AND cp.user.userId = :userId")
  void updateLastReadAt(
      @Param("conversationId") Long conversationId,
      @Param("userId") Long userId,
      @Param("lastReadAt") LocalDateTime lastReadAt
  );

  boolean existsByConversationConversationIdAndUserUserId(Long conversationId, Long userId);
}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Conversation;
import com.quocchung.cntt1.techcycle_system.model.ConversationParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  @Query("SELECT c FROM Conversation c " +
         "LEFT JOIN FETCH c.post " +
         "LEFT JOIN FETCH c.createdBy " +
         "WHERE c.conversationId = :id")
  Optional<Conversation> findByIdWithDetails(@Param("id") Long id);

  @Query("SELECT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "LEFT JOIN FETCH c.post " +
         "LEFT JOIN FETCH c.createdBy " +
         "WHERE cp.user.userId = :userId " +
         "ORDER BY c.lastMessageAt DESC NULLS LAST")
  Page<Conversation> findByParticipantUserIdOrderByLastMessageAtDesc(
      @Param("userId") Long userId,
      Pageable pageable
  );

  @Query("SELECT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "WHERE cp.user.userId = :userId " +
         "AND c.post.postId = :postId " +
         "AND cp.user.userId != :creatorId")
  Optional<Conversation> findExistingConversation(
      @Param("userId") Long userId,
      @Param("postId") Long postId,
      @Param("creatorId") Long creatorId
  );

  @Query("SELECT c FROM Conversation c " +
         "WHERE c.post IS NULL " +
         "AND (SELECT COUNT(cp) FROM ConversationParticipant cp WHERE cp.conversation = c) = :size " +
         "AND :userId1 IN (SELECT cp1.user.userId FROM ConversationParticipant cp1 WHERE cp1.conversation = c) " +
         "AND :userId2 IN (SELECT cp2.user.userId FROM ConversationParticipant cp2 WHERE cp2.conversation = c)")
  Optional<Conversation> findDirectConversation(
      @Param("userId1") Long userId1,
      @Param("userId2") Long userId2,
      @Param("size") int size
  );

  @Query("SELECT DISTINCT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "WHERE cp.user.userId = :userId")
  List<Conversation> findAllByParticipantUserId(@Param("userId") Long userId);

  @Query("SELECT c FROM Conversation c " +
         "LEFT JOIN FETCH c.post " +
         "LEFT JOIN FETCH c.createdBy " +
         "WHERE c.conversationId IN :ids")
  List<Conversation> findAllByIdsWithDetails(@Param("ids") List<Long> ids);
}

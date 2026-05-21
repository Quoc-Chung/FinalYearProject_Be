package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query("SELECT m FROM Message m " +
         "JOIN FETCH m.sender " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.deletedAt IS NULL " +
         "ORDER BY m.createdAt DESC")
  Page<Message> findByConversationIdOrderByCreatedAtDesc(
      @Param("conversationId") Long conversationId,
      Pageable pageable
  );

  @Query("SELECT m FROM Message m " +
         "JOIN FETCH m.sender " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.deletedAt IS NULL " +
         "ORDER BY m.createdAt DESC " +
         "LIMIT 1")
  Optional<Message> findLastMessageByConversationId(@Param("conversationId") Long conversationId);

  @Query("SELECT m FROM Message m " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.sender.userId = :senderId " +
         "AND m.deletedAt IS NULL " +
         "ORDER BY m.createdAt DESC " +
         "LIMIT 1")
  Optional<Message> findLastMessageByConversationAndSender(
      @Param("conversationId") Long conversationId,
      @Param("senderId") Long senderId
  );

  @Query("SELECT COUNT(m) FROM Message m " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.isRead = false " +
         "AND m.sender.userId != :userId " +
         "AND m.deletedAt IS NULL")
  long countUnreadMessages(
      @Param("conversationId") Long conversationId,
      @Param("userId") Long userId
  );

  @Modifying
  @Query("UPDATE Message m " +
         "SET m.isRead = true " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.sender.userId != :userId " +
         "AND m.isRead = false " +
         "AND m.deletedAt IS NULL")
  void markMessagesAsRead(
      @Param("conversationId") Long conversationId,
      @Param("userId") Long userId
  );

  @Query("SELECT m FROM Message m " +
         "WHERE m.conversation.conversationId = :conversationId " +
         "AND m.createdAt > :since " +
         "AND m.deletedAt IS NULL")
  List<Message> findMessagesSince(
      @Param("conversationId") Long conversationId,
      @Param("since") LocalDateTime since
  );
}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Conversation;
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

  /**
   * Lấy danh sách người dùng đã từng nhắn tin với userId, sắp xếp theo thời gian tin nhắn cuối cùng.
   * Loại trừ chính userId ra khỏi kết quả.
   *
   * @param userId ID của người dùng hiện tại
   * @param pageable Thông tin phân trang
   * @return Danh sách Object[] chứa [User, lastMessageAt]
   */
  @Query("""
      SELECT m.sender, MAX(m.createdAt) as lastMessageAt
      FROM Message m
      INNER JOIN ConversationParticipant cp ON cp.conversation = m.conversation
      WHERE cp.user.userId = :userId
        AND m.sender.userId != :userId
      GROUP BY m.sender.userId
      ORDER BY lastMessageAt DESC
      """)
  Page<Object[]> findChatContactsByUserId(@Param("userId") Long userId, Pageable pageable);

  /**
   * Tìm cuộc trò chuyện theo ID kèm thông tin người tạo.
   */
  @Query("SELECT c FROM Conversation c " +
         "LEFT JOIN FETCH c.createdBy " +
         "WHERE c.conversationId = :id")
  Optional<Conversation> findByIdWithDetails(@Param("id") Long id);

  /**
   * Tìm tất cả cuộc trò chuyện của một người dùng, sắp xếp theo thời gian tin nhắn cuối.
   */
  @Query("SELECT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "LEFT JOIN FETCH c.createdBy " +
         "WHERE cp.user.userId = :userId " +
         "ORDER BY c.lastMessageAt DESC NULLS LAST")
  Page<Conversation> findByParticipantUserIdOrderByLastMessageAtDesc(
      @Param("userId") Long userId,
      Pageable pageable
  );

  /**
   * Tìm cuộc trò chuyện trực tiếp giữa 2 người dùng (chat 1-1).
   */
  @Query("SELECT c FROM Conversation c " +
         "WHERE c.conversationId IN " +
         "  (SELECT cp1.conversation.conversationId FROM ConversationParticipant cp1 " +
         "   WHERE cp1.user.userId = :userId1 " +
         "   AND cp1.conversation.conversationId IN " +
         "     (SELECT cp2.conversation.conversationId FROM ConversationParticipant cp2 " +
         "      WHERE cp2.user.userId = :userId2)) " +
         "AND (SELECT COUNT(cp) FROM ConversationParticipant cp WHERE cp.conversation = c) = :size")
  Optional<Conversation> findDirectConversation(
      @Param("userId1") Long userId1,
      @Param("userId2") Long userId2,
      @Param("size") int size
  );
  /**
   * Lấy tất cả cuộc trò chuyện cho admin, sắp xếp theo thời gian tin nhắn cuối.
   * Chỉ lấy các cuộc trò chuyện giữa user và admin.
   */
  @Query("SELECT DISTINCT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "INNER JOIN cp.user u " +
         "INNER JOIN UserRole ur ON ur.user = u " +
         "INNER JOIN ur.role r " +
         "WHERE r.name = 'ADMIN' " +
         "ORDER BY c.lastMessageAt DESC NULLS LAST")
  Page<Conversation> findAllConversationsForAdmin(Pageable pageable);

  /**
   * Lấy tất cả cuộc trò chuyện của một admin cụ thể.
   * Chỉ lấy các cuộc trò chuyện giữa user và admin đó (loại bỏ admin-admin).
   */
  @Query("SELECT DISTINCT c FROM Conversation c " +
         "INNER JOIN ConversationParticipant cp ON cp.conversation = c " +
         "WHERE cp.user.userId = :adminId " +
         "AND EXISTS (SELECT cp2 FROM ConversationParticipant cp2 " +
         "            WHERE cp2.conversation = c " +
         "            AND cp2.user.userId != :adminId) " +
         "ORDER BY c.lastMessageAt DESC NULLS LAST")
  Page<Conversation> findConversationsByAdminId(@Param("adminId") Long adminId, Pageable pageable);

}

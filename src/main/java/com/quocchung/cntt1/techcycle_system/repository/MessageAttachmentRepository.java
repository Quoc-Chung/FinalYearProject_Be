package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

  List<MessageAttachment> findByMessageMessageId(Long messageId);

  @Query("SELECT a FROM MessageAttachment a WHERE a.message.messageId IN :messageIds")
  List<MessageAttachment> findByMessageIds(@Param("messageIds") List<Long> messageIds);
}

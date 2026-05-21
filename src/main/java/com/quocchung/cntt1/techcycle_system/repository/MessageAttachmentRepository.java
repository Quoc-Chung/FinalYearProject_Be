package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

  List<MessageAttachment> findByMessageMessageId(Long messageId);

  void deleteByMessageMessageId(Long messageId);
}

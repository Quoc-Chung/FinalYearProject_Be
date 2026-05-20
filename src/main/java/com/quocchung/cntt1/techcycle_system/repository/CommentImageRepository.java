package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.CommentImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentImageRepository extends JpaRepository<CommentImage, Long> {

  List<CommentImage> findByCommentCommentIdOrderByCreatedAtAsc(Long commentId);

  void deleteByCommentCommentId(Long commentId);
}

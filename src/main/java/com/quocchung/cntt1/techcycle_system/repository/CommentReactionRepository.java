package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.CommentReaction;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

  Optional<CommentReaction> findByCommentCommentIdAndUserUserId(Long commentId, Long userId);

  void deleteByCommentCommentIdAndUserUserId(Long commentId, Long userId);

  void deleteByCommentCommentId(Long commentId);

  List<CommentReaction> findByCommentCommentId(Long commentId);

  long countByCommentCommentId(Long commentId);

  long countByCommentCommentIdAndReactionType(Long commentId, ReactionType reactionType);

  @Query("SELECT cr.reactionType, COUNT(cr) FROM CommentReaction cr WHERE cr.comment.commentId = :commentId GROUP BY cr.reactionType")
  List<Object[]> countReactionsByType(@Param("commentId") Long commentId);

  boolean existsByCommentCommentIdAndUserUserId(Long commentId, Long userId);
}

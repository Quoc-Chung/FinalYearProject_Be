package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByPostPostIdAndParentIsNullOrderByIsPinnedDescCreatedAtDesc(Long postId);

  List<Comment> findByPostPostIdAndParentCommentIdOrderByCreatedAtAsc(Long postId, Long parentId);

  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.commentId = :id")
  Optional<Comment> findByIdWithUser(@Param("id") Long id);

  @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.postId = :postId")
  long countByPostId(@Param("postId") Long postId);

  @Modifying
  @Query("UPDATE Comment c SET c.likesCount = c.likesCount + 1 WHERE c.commentId = :commentId")
  void incrementLikesCount(@Param("commentId") Long commentId);

  @Modifying
  @Query("UPDATE Comment c SET c.likesCount = c.likesCount - 1 WHERE c.commentId = :commentId AND c.likesCount > 0")
  void decrementLikesCount(@Param("commentId") Long commentId);

  boolean existsByCommentIdAndUserUserId(Long commentId, Long userId);

  @Query("SELECT c.post.postId, COUNT(c) FROM Comment c WHERE c.post.postId IN :postIds GROUP BY c.post.postId")
  List<Object[]> countCommentsByPostIds(@Param("postIds") List<Long> postIds);
}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostReaction;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

  Optional<PostReaction> findByPostPostIdAndUserUserId(Long postId, Long userId);

  void deleteByPostPostIdAndUserUserId(Long postId, Long userId);

  List<PostReaction> findByPostPostId(Long postId);


  @Query("SELECT pr.reactionType, COUNT(pr) FROM PostReaction pr WHERE pr.post.postId = :postId GROUP BY pr.reactionType")
  List<Object[]> countReactionsByType(@Param("postId") Long postId);

  boolean existsByPostPostIdAndUserUserId(Long postId, Long userId);

  @Query("SELECT pr.post.postId, COUNT(pr) FROM PostReaction pr WHERE pr.post.postId IN :postIds GROUP BY pr.post.postId")
  List<Object[]> countReactionsByPostIds(@Param("postIds") List<Long> postIds);

  @Query("SELECT pr.post.postId, pr.reactionType, COUNT(pr) FROM PostReaction pr WHERE pr.post.postId IN :postIds GROUP BY pr.post.postId, pr.reactionType")
  List<Object[]> countReactionsByPostIdsGroupByType(@Param("postIds") List<Long> postIds);
}

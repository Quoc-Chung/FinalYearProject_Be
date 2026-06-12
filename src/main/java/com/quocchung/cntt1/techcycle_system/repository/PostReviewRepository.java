package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReviewRepository extends JpaRepository<PostReview, Long> {

    Page<PostReview> findByPostPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<PostReview> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT AVG(pr.rating) FROM PostReview pr WHERE pr.post.postId = :postId")
    Double getAverageRatingByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(pr) FROM PostReview pr WHERE pr.post.postId = :postId")
    long countByPostId(@Param("postId") Long postId);

    boolean existsByPostPostIdAndUserUserId(Long postId, Long userId);
}

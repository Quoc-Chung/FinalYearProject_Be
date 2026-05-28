package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserReview;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

  Optional<UserReview> findByFromUserAndToUser(User fromUser, User toUser);

  @Query("SELECT r FROM UserReview r JOIN FETCH r.fromUser WHERE r.toUser.userId = :userId")
  Page<UserReview> findByToUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT AVG(r.rating) FROM UserReview r WHERE r.toUser.userId = :userId")
  Double getAverageRatingByUserId(@Param("userId") Long userId);

  default Double findAverageRating(Long userId) {
    return getAverageRatingByUserId(userId);
  }

  @Query("SELECT COUNT(r) FROM UserReview r WHERE r.toUser.userId = :userId")
  long countByToUserId(@Param("userId") Long userId);
}
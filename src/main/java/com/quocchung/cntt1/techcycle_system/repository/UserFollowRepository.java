package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserFollow;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

  Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

  boolean existsByFollowerAndFollowing(User follower, User following);

  @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.userId = :userId")
  long countFollowersByUserId(@Param("userId") Long userId);

  @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.userId = :userId")
  long countFollowingByUserId(@Param("userId") Long userId);

  @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.following WHERE uf.follower.userId = :userId")
  Page<UserFollow> findFollowingByFollowerId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.follower WHERE uf.following.userId = :userId")
  Page<UserFollow> findFollowersByFollowingId(@Param("userId") Long userId, Pageable pageable);
}

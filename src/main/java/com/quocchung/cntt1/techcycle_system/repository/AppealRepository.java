package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Appeal;
import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

  Page<Appeal> findByStatus(AppealStatus status, Pageable pageable);

  Page<Appeal> findAll(Pageable pageable);

  Optional<Appeal> findByPostPostIdAndUserUserId(Long postId, Long userId);

  boolean existsByPostPostIdAndUserUserId(Long postId, Long userId);

  @Query("SELECT a FROM Appeal a WHERE a.post.postId = :postId")
  Page<Appeal> findByPostId(@Param("postId") Long postId, Pageable pageable);

  @Query("SELECT a FROM Appeal a WHERE a.user.userId = :userId")
  Page<Appeal> findByUserId(@Param("userId") Long userId, Pageable pageable);

  long countByStatus(AppealStatus status);
}

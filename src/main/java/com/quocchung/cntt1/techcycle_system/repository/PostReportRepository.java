package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {

  @Query("SELECT COUNT(pr) FROM PostReport pr " +
         "WHERE pr.post.user.userId = :userId " +
         "AND pr.status = :status")
  long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReportStatus status);

  @Query("SELECT COUNT(pr) FROM PostReport pr " +
         "WHERE pr.post.user.userId = :userId " +
         "AND pr.status = 'VIOLATION'")
  long countViolationReportsByUserId(@Param("userId") Long userId);


  Page<PostReport> findByReporterUserId(Long reporterId, Pageable pageable);

  Page<PostReport> findByPostPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

  boolean existsByPostPostIdAndReporterUserId(Long postId, Long reporterId);

  Page<PostReport> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
}

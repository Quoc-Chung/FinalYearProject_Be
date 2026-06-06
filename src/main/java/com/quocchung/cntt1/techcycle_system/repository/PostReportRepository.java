package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {

  @Query("SELECT COUNT(DISTINCT pr.post.postId) FROM PostReport pr "
      + "WHERE pr.post.user.userId = :userId "
      + "AND pr.post.status = 'VIOLATION'")
  long countViolationReportsByUserId(@Param("userId") Long userId);

  Page<PostReport> findByReporterUserId(Long reporterId, Pageable pageable);

  Page<PostReport> findByPostPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

  boolean existsByPostPostIdAndReporterUserId(Long postId, Long reporterId);

  Page<PostReport> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

  @Query("SELECT pr FROM PostReport pr "
      + "JOIN FETCH pr.post p "
      + "JOIN FETCH p.user u "
      + "JOIN FETCH pr.reporter r "
      + "LEFT JOIN FETCH pr.resolvedBy rb")
  Page<PostReport> findAllForAdmin(Pageable pageable);

  @Query("SELECT pr FROM PostReport pr "
      + "JOIN FETCH pr.post p "
      + "JOIN FETCH p.user u "
      + "JOIN FETCH pr.reporter r "
      + "LEFT JOIN FETCH pr.resolvedBy rb "
      + "WHERE pr.status = :status")
  Page<PostReport> findAllForAdminByStatus(@Param("status") ReportStatus status, Pageable pageable);

  @Query("SELECT COUNT(DISTINCT pr.reporter.userId) FROM PostReport pr "
      + "WHERE pr.post.postId = :postId "
      + "AND pr.status = :status")
  long countDistinctReporterByPostIdAndStatus(@Param("postId") Long postId,
                                              @Param("status") ReportStatus status);

  @Query("SELECT COUNT(DISTINCT p.postId) FROM PostReport pr "
      + "JOIN pr.post p "
      + "WHERE p.user.userId = :userId "
      + "AND p.status = 'VIOLATION' "
      + "AND p.updatedAt >= :since")
  long countViolationPostsByOwnerSince(@Param("userId") Long userId,
                                       @Param("since") LocalDateTime since);

  @Modifying
  @Query("UPDATE PostReport r "
      + "SET r.status = :newStatus, "
      + "r.resolvedBy = :admin, "
      + "r.resolvedAt = :resolvedAt "
      + "WHERE r.post.postId = :postId "
      + "AND r.status = 'PENDING'")
  int resolvePendingReportsByPostId(@Param("postId") Long postId,
                                    @Param("newStatus") ReportStatus newStatus,
                                    @Param("admin") com.quocchung.cntt1.techcycle_system.model.User admin,
                                    @Param("resolvedAt") LocalDateTime resolvedAt);

  List<PostReport> findByPostPostIdAndStatus(Long postId, ReportStatus status);
}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostView;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Post, Long> {

    // ========== POST STATS ==========
    
    // Đếm tổng số bài viết
    @Query("SELECT COUNT(p) FROM Post p WHERE p.deletedAt IS NULL")
    Long countTotalPosts();
    
    // Đếm bài viết mới trong ngày
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfDay AND p.deletedAt IS NULL")
    Long countPostsToday(@Param("startOfDay") LocalDateTime startOfDay);
    
    // Đếm bài viết theo trạng thái
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status AND p.deletedAt IS NULL")
    Long countPostsByStatus(@Param("status") PostStatus status);
    
    // Đếm bài viết theo trạng thái trong ngày
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status AND p.createdAt >= :startOfDay AND p.deletedAt IS NULL")
    Long countPostsByStatusToday(@Param("status") PostStatus status, @Param("startOfDay") LocalDateTime startOfDay);

    // Lấy bài viết mới nhất (cho danh sách gần đây)
    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(org.springframework.data.domain.Pageable pageable);
    
    // Đếm bài viết trong khoảng thời gian
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :start AND :end AND p.deletedAt IS NULL")
    Long countPostsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Đếm bài viết đã duyệt trong khoảng thời gian
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = 'APPROVED' AND p.approvedAt BETWEEN :start AND :end AND p.deletedAt IS NULL")
    Long countApprovedPostsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== USER STATS ==========
    @Query("""
    SELECT COUNT(u)
    FROM User u
    JOIN UserRole ur ON ur.user = u
    JOIN ur.role rr
    WHERE rr.name <> 'ADMIN'
    AND u.status = 'ACTIVE'
""")
    Long countTotalUsers();
    
    // Đếm người dùng mới trong ngày
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay AND u.deletedAt IS NULL")
    Long countUsersToday(@Param("startOfDay") LocalDateTime startOfDay);
    
    // Đếm người dùng theo trạng thái
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    Long countUsersByStatus(@Param("status") UserStatus status);

    // Đếm người dùng đã xóa
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NOT NULL")
    Long countDeletedUsers();
    
    // Lấy người dùng mới nhất
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(org.springframework.data.domain.Pageable pageable);
    
    // Đếm người dùng trong khoảng thời gian
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.deletedAt IS NULL")
    Long countUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== VIEW STATS ==========
    
    // Đếm tổng lượt xem
    @Query("SELECT COUNT(v) FROM PostView v")
    Long countTotalViews();
    
    // Đếm lượt xem trong ngày
    @Query("SELECT COUNT(v) FROM PostView v WHERE v.viewedAt >= :startOfDay")
    Long countViewsToday(@Param("startOfDay") LocalDateTime startOfDay);
    
    // Đếm lượt xem trong khoảng thời gian
    @Query("SELECT COUNT(v) FROM PostView v WHERE v.viewedAt BETWEEN :start AND :end")
    Long countViewsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== REPORT STATS ==========
    
    // Đếm tổng báo cáo
    @Query("SELECT COUNT(r) FROM PostReport r")
    Long countTotalReports();
    
    // Đếm báo cáo đang chờ
    @Query("SELECT COUNT(r) FROM PostReport r WHERE r.status = :status")
    Long countReportsByStatus(@Param("status") ReportStatus status);
    
    // ========== CATEGORY STATS ==========
    
    // Đếm bài viết theo danh mục
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.categoryId = :categoryId AND p.deletedAt IS NULL")
    Long countPostsByCategory(@Param("categoryId") Long categoryId);
    
    // Đếm bài viết đã duyệt theo danh mục
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.categoryId = :categoryId AND p.status = 'APPROVED' AND p.deletedAt IS NULL")
    Long countApprovedPostsByCategory(@Param("categoryId") Long categoryId);
    
    // Đếm bài viết đã bán theo danh mục
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.categoryId = :categoryId AND p.status = 'SOLD' AND p.deletedAt IS NULL")
    Long countSoldPostsByCategory(@Param("categoryId") Long categoryId);
    
    // Đếm bài viết chờ duyệt theo danh mục
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.categoryId = :categoryId AND p.status = 'PENDING' AND p.deletedAt IS NULL")
    Long countPendingPostsByCategory(@Param("categoryId") Long categoryId);

    // Đếm bài viết vi phạm
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = 'VIOLATION' AND p.deletedAt IS NULL")
    Long countViolatedPosts();
}

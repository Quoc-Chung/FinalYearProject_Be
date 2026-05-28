package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard.*;
import com.quocchung.cntt1.techcycle_system.service.DashboardService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashBoardController {

    private final DashboardService dashboardService;
    private final ResponseUtils responseUtils;

    /**
     * GET /api/dashboard
     * Lấy toàn bộ dashboard bao gồm: stats, weeklyStats, categoryStats, recentPosts, recentUsers
     */
    @GetMapping
    public ResponseEntity<?> getFullDashboard() {
        DashboardResponse dashboard = dashboardService.getFullDashboard();
        return ResponseEntity.ok(responseUtils.success(dashboard));
    }

    /**
     * GET /api/dashboard/stats
     * Lấy các số liệu thống kê tổng quan
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(responseUtils.success(stats));
    }

    /**
     * GET /api/dashboard/weekly
     * Lấy số liệu theo ngày trong 7 ngày gần nhất
     */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStats() {
        List<DailyStatsResponse> weeklyStats = dashboardService.getWeeklyStats();
        return ResponseEntity.ok(responseUtils.successList(weeklyStats));
    }

    /**
     * GET /api/dashboard/categories
     * Lấy số liệu bài viết theo danh mục
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategoryStats() {
        List<CategoryStatsResponse> categoryStats = dashboardService.getCategoryStats();
        return ResponseEntity.ok(responseUtils.successList(categoryStats));
    }

    /**
     * GET /api/dashboard/posts?limit=5
     * Lấy danh sách bài viết gần đây
     */
    @GetMapping("/posts")
    public ResponseEntity<?> getRecentPosts(
            @RequestParam(defaultValue = "5") int limit) {
        List<RecentPostResponse> recentPosts = dashboardService.getRecentPosts(limit);
        return ResponseEntity.ok(responseUtils.successList(recentPosts));
    }

    /**
     * GET /api/dashboard/users?limit=5
     * Lấy danh sách người dùng mới gần đây
     */
    @GetMapping("/users")
    public ResponseEntity<?> getRecentUsers(
            @RequestParam(defaultValue = "5") int limit) {
        List<RecentUserResponse> recentUsers = dashboardService.getRecentUsers(limit);
        return ResponseEntity.ok(responseUtils.successList(recentUsers));
    }
}

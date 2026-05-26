package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsResponse {
    // Tổng số bài viết
    private Long totalPosts;

    // Bài viết mới trong ngày
    private Long newPostsToday;

    // Tổng số người dùng
    private Long totalUsers;

    // Người dùng mới trong ngày
    private Long newUsersToday;

    // Tổng lượt xem (tất cả bài viết)
    private Long totalViews;

    // Lượt xem trong ngày
    private Long viewsToday;

    // Tổng bài chờ duyệt
    private Long pendingPosts;

    // Tổng bài đã duyệt
    private Long approvedPosts;

    // Tổng bài đã bán
    private Long soldPosts;

    // Tổng bài bị từ chối
    private Long rejectedPosts;

    // Tổng bài vi phạm
    private Long violatedPosts;

    // Tổng số báo cáo
    private Long totalReports;

    // Báo cáo đang chờ xử lý
    private Long pendingReports;

    // So sánh với ngày hôm qua (% tăng/giảm)
    private Double postsGrowth;
    private Double usersGrowth;
    private Double viewsGrowth;
}

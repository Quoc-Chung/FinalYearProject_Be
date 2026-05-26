package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
    // Tổng quan
    private DashboardStatsResponse stats;
    
    // Biểu đồ đường/cột - 7 ngày gần nhất
    private List<DailyStatsResponse> weeklyStats;
    
    // Biểu đồ tròn - theo danh mục
    private List<CategoryStatsResponse> categoryStats;
    
    // Bài viết mới gần đây
    private List<RecentPostResponse> recentPosts;
    
    // Người dùng mới gần đây
    private List<RecentUserResponse> recentUsers;
}

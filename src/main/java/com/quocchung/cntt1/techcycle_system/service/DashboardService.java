package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard.*;

public interface DashboardService {
    
    DashboardResponse getFullDashboard();
    
    DashboardStatsResponse getStats();
    
    java.util.List<DailyStatsResponse> getWeeklyStats();
    
    java.util.List<CategoryStatsResponse> getCategoryStats();
    
    java.util.List<RecentPostResponse> getRecentPosts(int limit);
    
    java.util.List<RecentUserResponse> getRecentUsers(int limit);
}

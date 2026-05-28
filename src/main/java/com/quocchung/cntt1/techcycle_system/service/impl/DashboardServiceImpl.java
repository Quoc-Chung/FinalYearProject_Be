package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard.*;
import com.quocchung.cntt1.techcycle_system.model.*;
import com.quocchung.cntt1.techcycle_system.repository.*;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements com.quocchung.cntt1.techcycle_system.service.DashboardService {

    private final DashboardRepository dashboardRepository;
    private final com.quocchung.cntt1.techcycle_system.repository.PostRepository postRepository;
    private final com.quocchung.cntt1.techcycle_system.repository.UserRepository userRepository;
    private final com.quocchung.cntt1.techcycle_system.repository.CategoryRepository categoryRepository;

    // ========== MAIN DASHBOARD ==========
    
    @Override
    public DashboardResponse getFullDashboard() {
        return DashboardResponse.builder()
                .stats(getStats())
                .weeklyStats(getWeeklyStats())
                .categoryStats(getCategoryStats())
                .recentPosts(getRecentPosts(5))
                .recentUsers(getRecentUsers(5))
                .build();
    }

    // ========== STATS ==========
    
    @Override
    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfYesterday = now.toLocalDate().minusDays(1).atStartOfDay();
        LocalDateTime startOfLastWeek = now.toLocalDate().minusDays(7).atStartOfDay();

        // Posts stats
        Long totalPosts = dashboardRepository.countTotalPosts();
        Long newPostsToday = dashboardRepository.countPostsToday(startOfToday);
        Long postsYesterday = dashboardRepository.countPostsBetween(startOfYesterday, startOfToday);

        // User stats
        Long totalUsers = dashboardRepository.countTotalUsers();
        Long newUsersToday = dashboardRepository.countUsersToday(startOfToday);
        Long usersYesterday = dashboardRepository.countUsersBetween(startOfYesterday, startOfToday);

        // View stats
        Long totalViews = dashboardRepository.countTotalViews();
        Long viewsToday = dashboardRepository.countViewsToday(startOfToday);
        Long viewsYesterday = dashboardRepository.countViewsBetween(startOfYesterday, startOfToday);

        // Post status counts
        Long pendingPosts = dashboardRepository.countPostsByStatus(PostStatus.PENDING);
        Long approvedPosts = dashboardRepository.countPostsByStatus(PostStatus.APPROVED);
        Long soldPosts = dashboardRepository.countPostsByStatus(PostStatus.SOLD);
        Long rejectedPosts = dashboardRepository.countPostsByStatus(PostStatus.REJECTED);
        Long violatedPosts = dashboardRepository.countViolatedPosts();

        // Report stats
        Long totalReports = dashboardRepository.countTotalReports();
        Long pendingReports = dashboardRepository.countReportsByStatus(ReportStatus.PENDING);

        // Calculate growth rates
        Double postsGrowth = calculateGrowth(newPostsToday, postsYesterday);
        Double usersGrowth = calculateGrowth(newUsersToday, usersYesterday);
        Double viewsGrowth = calculateGrowth(viewsToday, viewsYesterday);

        return DashboardStatsResponse.builder()
                .totalPosts(totalPosts)
                .newPostsToday(newPostsToday)
                .totalUsers(totalUsers)
                .newUsersToday(newUsersToday)
                .totalViews(totalViews)
                .viewsToday(viewsToday)
                .pendingPosts(pendingPosts)
                .approvedPosts(approvedPosts)
                .soldPosts(soldPosts)
                .rejectedPosts(rejectedPosts)
                .violatedPosts(violatedPosts)
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .postsGrowth(postsGrowth)
                .usersGrowth(usersGrowth)
                .viewsGrowth(viewsGrowth)
                .build();
    }

    private Double calculateGrowth(Long today, Long yesterday) {
        if (yesterday == null || yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        double growth = ((today - yesterday) / (double) yesterday) * 100;
        return Math.round(growth * 10.0) / 10.0;
    }

    // ========== WEEKLY STATS ==========
    
    @Override
    public List<DailyStatsResponse> getWeeklyStats() {
        List<DailyStatsResponse> weeklyStats = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Get stats for last 7 days
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            // Count posts created on this day
            Long posts = dashboardRepository.countPostsBetween(startOfDay, endOfDay);
            
            // Count users created on this day
            Long newUsers = dashboardRepository.countUsersBetween(startOfDay, endOfDay);
            
            // Count views on this day
            Long views = dashboardRepository.countViewsBetween(startOfDay, endOfDay);

            // Get day name in Vietnamese
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("vi", "VN"));
            dayName = convertToVietnameseDay(dayName, date);

            weeklyStats.add(DailyStatsResponse.builder()
                    .dayName(dayName)
                    .date(date.toString())
                    .posts(posts != null ? posts : 0L)
                    .newUsers(newUsers != null ? newUsers : 0L)
                    .views(views != null ? views : 0L)
                    .approvedPosts(0L)  // Not tracking per-day approved in this query
                    .soldPosts(0L)
                    .build());
        }

        return weeklyStats;
    }

    private String convertToVietnameseDay(String dayName, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        switch (dow) {
            case MONDAY: return "T2";
            case TUESDAY: return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY: return "T5";
            case FRIDAY: return "T6";
            case SATURDAY: return "T7";
            case SUNDAY: return "CN";
            default: return dayName;
        }
    }

    // ========== CATEGORY STATS ==========
    
    @Override
    public List<CategoryStatsResponse> getCategoryStats() {
        List<Category> categories = categoryRepository.findAll();
        Long totalPosts = dashboardRepository.countTotalPosts();

        List<CategoryStatsResponse> categoryStats = categories.stream()
                .filter(cat -> cat.getIsActive() != null && cat.getIsActive())
                .map(cat -> {
                    Long postCount = dashboardRepository.countPostsByCategory(cat.getCategoryId());
                    Long approvedCount = dashboardRepository.countApprovedPostsByCategory(cat.getCategoryId());
                    Long soldCount = dashboardRepository.countSoldPostsByCategory(cat.getCategoryId());
                    Long pendingCount = dashboardRepository.countPendingPostsByCategory(cat.getCategoryId());

                    postCount = postCount != null ? postCount : 0L;
                    approvedCount = approvedCount != null ? approvedCount : 0L;
                    soldCount = soldCount != null ? soldCount : 0L;
                    pendingCount = pendingCount != null ? pendingCount : 0L;

                    Double percentage = 0.0;
                    if (totalPosts != null && totalPosts > 0) {
                        percentage = (postCount.doubleValue() / totalPosts.doubleValue()) * 100;
                        percentage = Math.round(percentage * 10.0) / 10.0;
                    }

                    return CategoryStatsResponse.builder()
                            .categoryId(cat.getCategoryId())
                            .categoryName(cat.getName())
                            .postCount(postCount)
                            .approvedCount(approvedCount)
                            .soldCount(soldCount)
                            .pendingCount(pendingCount)
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getPostCount().compareTo(a.getPostCount()))
                .collect(Collectors.toList());

        return categoryStats;
    }

    // ========== RECENT POSTS ==========

    @Override
    public List<RecentPostResponse> getRecentPosts(int limit) {
        List<Post> posts = postRepository.findTop20ByDeletedAtIsNullOrderByCreatedAtDesc();
        // Apply limit
        if (posts.size() > limit) {
            posts = posts.subList(0, limit);
        }

        return posts.stream().map(post -> {
            String price = "";
            if (post.getPrice() != null) {
                price = formatPrice(post.getPrice());
            }

            return RecentPostResponse.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .authorName(post.getUser() != null ? post.getUser().getFullName() : "Unknown")
                    .authorAvatar(post.getUser() != null ? post.getUser().getAvatarUrl() : null)
                    .categoryName(post.getCategory() != null ? post.getCategory().getName() : "Unknown")
                    .price(price)
                    .status(post.getStatus() != null ? post.getStatus().toString() : "UNKNOWN")
                    .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : "")
                    .viewCount(0L)  // Would need separate query
                    .likeCount(0L)  // Would need separate query
                    .commentCount(0L)  // Would need separate query
                    .build();
        }).collect(Collectors.toList());
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "";
        return String.format("%,.0fđ", price);
    }

    // ========== RECENT USERS ==========
    
    @Override
    public List<RecentUserResponse> getRecentUsers(int limit) {
        List<User> users = userRepository.findRecentUsers(PageRequest.of(0, limit));

        return users.stream().map(user -> {
            Long postCount = postRepository.countByUserUserId(user.getUserId());

            return RecentUserResponse.builder()
                    .userId(user.getUserId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .avatarUrl(user.getAvatarUrl())
                    .status(user.getStatus() != null ? user.getStatus().toString() : "UNKNOWN")
                    .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                    .postCount(postCount != null ? postCount : 0L)
                    .trustScore(user.getTrustScore() != null ? user.getTrustScore() : 0.0)
                    .build();
        }).collect(Collectors.toList());
    }
}

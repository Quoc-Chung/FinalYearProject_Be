package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyStatsResponse {
    private String dayName;      // "T2", "T3", "T4", "T5", "T6", "T7", "CN"
    private String date;         // "2026-05-20"
    private Long posts;          // Số bài viết mới trong ngày
    private Long newUsers;       // Số người dùng mới trong ngày
    private Long views;          // Số lượt xem trong ngày
    private Long approvedPosts;  // Số bài được duyệt trong ngày
    private Long soldPosts;      // Số bài bán được trong ngày
}

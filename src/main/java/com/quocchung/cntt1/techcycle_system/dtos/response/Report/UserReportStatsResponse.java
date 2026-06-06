package com.quocchung.cntt1.techcycle_system.dtos.response.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReportStatsResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Long totalPosts;
    private Long approvedPosts;
    private Long rejectedPosts;
    private Long pendingPosts;
    private Long followerCount;
    private Long followingCount;
}
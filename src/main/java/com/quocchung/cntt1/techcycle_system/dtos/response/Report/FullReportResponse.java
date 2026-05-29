package com.quocchung.cntt1.techcycle_system.dtos.response.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullReportResponse {
    private Map<String, Long> postStatusSummary;
    private Map<String, Long> userStatusSummary;
    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    private Long totalReactions;
    private Long totalFollowers;
}

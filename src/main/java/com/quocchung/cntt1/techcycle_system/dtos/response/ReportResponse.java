package com.quocchung.cntt1.techcycle_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long reportId;
    private Long postId;
    private String postTitle;
    private ReporterInfo reporter;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporterInfo {
        private Long userId;
        private String fullName;
        private String avatarUrl;
    }
}

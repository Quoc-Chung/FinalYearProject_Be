package com.quocchung.cntt1.techcycle_system.dtos.response;

import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealResponse {

    private Long appealId;
    private Long postId;
    private String postTitle;
    private String postThumbnail;
    private UserInfo user;
    private String reason;
    private String adminNote;
    private AppealStatus status;
    private AdminInfo processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInfo {
        private Long userId;
        private String fullName;
    }
}

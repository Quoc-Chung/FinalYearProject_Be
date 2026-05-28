package com.quocchung.cntt1.techcycle_system.dtos.response.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReviewResponse {

    private Long reviewId;
    private Long postId;
    private String postTitle;
    private UserSummaryResponse user;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryResponse {
        private Long userId;
        private String fullName;
        private String avatarUrl;
    }
}

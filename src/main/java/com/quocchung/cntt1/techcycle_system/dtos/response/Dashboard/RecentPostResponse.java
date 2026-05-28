package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentPostResponse {
    private Long postId;
    private String title;
    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private String price;
    private String status;
    private String createdAt;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
}

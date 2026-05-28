package com.quocchung.cntt1.techcycle_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPostDetailResponse {
    private Long postId;
    private String title;
    private String description;
    private BigDecimal price;
    private String status;
    private String categoryName;
    private String brandName;
    private String conditionGrade;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long reactionCount;
    private Long commentCount;
}

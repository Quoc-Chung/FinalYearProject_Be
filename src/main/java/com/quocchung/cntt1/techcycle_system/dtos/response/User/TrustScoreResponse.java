package com.quocchung.cntt1.techcycle_system.dtos.response.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreResponse {

    private Long userId;
    private Double trustScore;
    private Double avgRating;
    private Integer reviewCount;
    private Long followerCount;
    private Long violationCount;
    private Double ratingBonus;
    private Double tenureBonus;
    private Double violationPenalty;
    private String scoreBreakdown;

    public static TrustScoreResponse from(
            Long userId,
            double trustScore,
            Double avgRating,
            Integer reviewCount,
            Long followerCount,
            Long violationCount,
            Double ratingBonus,
            Double tenureBonus,
            Double violationPenalty
    ) {
        String breakdown = String.format(
            "Điểm nền (50) + Rating (%.1f) + Thâm niên (%.1f) - Vi phạm (%.1f) = %.1f",
            ratingBonus, tenureBonus, violationPenalty, trustScore
        );

        return TrustScoreResponse.builder()
                .userId(userId)
                .trustScore(trustScore)
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .followerCount(followerCount)
                .violationCount(violationCount)
                .ratingBonus(ratingBonus)
                .tenureBonus(tenureBonus)
                .violationPenalty(violationPenalty)
                .scoreBreakdown(breakdown)
                .build();
    }
}

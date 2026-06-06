package com.quocchung.cntt1.techcycle_system.dtos.response.Seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponse {

    private Long userId;
    private String username;
    private String joinDate;
    private String trustScore;
    private SellerStats stats;
    private List<SellerListing> listings;
    private MeetLocation meetLocation;
    private List<String> sharedImages;
    private List<String> notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerStats {
        private int posts;           // Số bài đăng
        private int followers;       // Số người theo dõi
        private String avgRating;    // Điểm đánh giá trung bình (VD: "4.8")
        private int reviewCount;     // Số lượt đánh giá
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerListing {
        private Long postId;
        private String title;
        private BigDecimal price;
        private String formattedPrice;
        private String postedAt;
        private String thumbnailUrl;
        private String mediaType;
        private Long countReaction;
        private Long countComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetLocation {
        private String name;
        private String address;
    }
}

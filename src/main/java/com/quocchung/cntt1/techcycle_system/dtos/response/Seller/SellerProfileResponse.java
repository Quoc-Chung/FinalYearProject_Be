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
        private int exchanges;
        private int posts;
        private String responseRate;
        private String responseTime;
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
        private String image;
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

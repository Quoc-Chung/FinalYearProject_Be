package com.quocchung.cntt1.techcycle_system.dtos.response.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMetadataResponse {
   private Long trustScore;

   private Long countPost;

   // số lượng người theo dõi
   private Long countUserFollow;

   // điểm đánh giá
   private Double ratingScore;

   // số lượt đánh giá
   private Long reviewCount;
}

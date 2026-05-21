package com.quocchung.cntt1.techcycle_system.dtos.response.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class ReviewResponse {
  private Long reviewId;
  private UserSummaryResponse fromUser;
  private Integer rating;
  private String comment;
  private String tags;
  private String createdAt;
  private String updatedAt;
}
package com.quocchung.cntt1.techcycle_system.dtos.response.Review;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
  private Long reviewId;
  private UserSummaryResponse fromUser;
  private Integer rating;
  private String comment;
  private List<String> tags;
  private Long transactionId;
  private String createdAt;
  private String updatedAt;
}
package com.quocchung.cntt1.techcycle_system.dtos.request.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

  @NotNull(message = "Rating không được để trống")
  @Min(value = 1, message = "Rating tối thiểu là 1")
  @Max(value = 5, message = "Rating tối đa là 5")
  private Integer rating;

  private String comment;

  private List<String> tags;

  private Long transactionId;
}
package com.quocchung.cntt1.techcycle_system.dtos.response.SearchHistoryResponse;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryResponse {
  private Long searchId;
  private String keyword;
  private Integer resultCount;
  private LocalDateTime createdAt;
}
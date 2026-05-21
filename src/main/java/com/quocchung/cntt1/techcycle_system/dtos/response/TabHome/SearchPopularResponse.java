package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchPopularResponse {
  private String keyword;
  private Integer resultCount;
}

package com.quocchung.cntt1.techcycle_system.utils.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse {
  @JsonProperty("total_pages")
  private Integer totalPages;

  @JsonProperty("has_next")
  private Boolean hasNext;

  @JsonProperty("has_previous")
  private Boolean hasPrevious;

  @JsonProperty("current_page")
  private Integer currentPage;

  @JsonProperty("total_elements")
  private Long totalElements;
}

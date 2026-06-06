package com.quocchung.cntt1.techcycle_system.dtos.request.SearchHistoryRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryRequest {
  @NotBlank(message = "Keyword không được để trống")
  @Size(max = 255, message = "Keyword tối đa 255 ký tự")
  private String keyword;

  private Integer resultCount;
}
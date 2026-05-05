package com.quocchung.cntt1.techcycle_system.dtos.request.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
  private String searchText;
  private String status;
  private Integer page = 1;
  private Integer size = 10;
}

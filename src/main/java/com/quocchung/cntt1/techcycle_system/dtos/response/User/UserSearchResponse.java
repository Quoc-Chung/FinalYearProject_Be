package com.quocchung.cntt1.techcycle_system.dtos.response.User;

import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
  private List<UserResponse> users;
  private Long totalElements;
}

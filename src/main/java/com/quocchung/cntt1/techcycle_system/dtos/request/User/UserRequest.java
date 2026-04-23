package com.quocchung.cntt1.techcycle_system.dtos.request.User;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
  @Size(max = 150, message = "fullName tối đa 150 ký tự")
  private String fullName;

  @Size(max = 20, message = "phone tối đa 20 ký tự")
  private String phone;

  @Size(max = 1000, message = "bio tối đa 1000 ký tự")
  private String bio;

  private MultipartFile avatar;
}

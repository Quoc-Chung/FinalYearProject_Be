package com.quocchung.cntt1.techcycle_system.dtos.request.User;

import jakarta.persistence.Column;
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
  private String fullName;

  private String phone;

  private String bio;

  private String province;

  private Long addressId;

  private MultipartFile avatar;
}

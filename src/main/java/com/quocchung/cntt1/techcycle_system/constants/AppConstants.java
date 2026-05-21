package com.quocchung.cntt1.techcycle_system.constants;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class AppConstants {

  public static final String DEVICE_HEADER = "X-Device-Id";
  public static final String DEFAULT_DEVICE_ID = "web";
  public static final String DEFAULT_ROLE_NAME = "USER";

}

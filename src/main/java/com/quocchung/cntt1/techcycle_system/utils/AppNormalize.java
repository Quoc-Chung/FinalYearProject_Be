package com.quocchung.cntt1.techcycle_system.utils;

import static com.quocchung.cntt1.techcycle_system.constants.AppConstants.DEFAULT_DEVICE_ID;

import org.springframework.stereotype.Component;

@Component
public class AppNormalize {
  public String normalizeDeviceId(String tokenDeviceId, String requestDeviceId) {
    if (tokenDeviceId != null && !tokenDeviceId.isBlank()) {
      return tokenDeviceId.trim();
    }
    return normalizeDeviceId(requestDeviceId);
  }
  /**
   *
   * @param deviceId
   * @return
   */
  public String normalizeDeviceId(String deviceId) {
    if (deviceId == null || deviceId.isBlank()) {
      return DEFAULT_DEVICE_ID;
    }
    return deviceId.trim();
  }
}
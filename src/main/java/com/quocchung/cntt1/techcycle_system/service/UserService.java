package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;

public interface UserService {
  UserResponse updateMe(String email, UserRequest request);
}

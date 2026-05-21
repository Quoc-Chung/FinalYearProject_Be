package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UpdateUserStatusRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserSearchRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserMetadataResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserSearchResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;

public interface UserService {
  UserResponse updateMe(String email, UserRequest request);

  void updateUserStatus(Long userId, UpdateUserStatusRequest request);

  UserSearchResponse searchUsers(UserSearchRequest request);

  UserMetadataResponse getUserMetadata(Long userId);
}

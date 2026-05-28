package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UpdateUserStatusRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserSearchRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserMetadataResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserSearchResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Seller.SellerProfileResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.TrustScoreResponse;

public interface UserService {
  UserResponse updateMe(String email, UserRequest request);

  void updateUserStatus(Long userId, UpdateUserStatusRequest request);

  UserSearchResponse searchUsers(UserSearchRequest request);

  UserMetadataResponse getUserMetadata(Long userId);

  SellerProfileResponse getSellerProfile(Long userId);

  TrustScoreResponse getTrustScore(Long userId);

  double calculateTrustScore(Long userId);
}

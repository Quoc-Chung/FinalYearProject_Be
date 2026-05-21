package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.dtos.response.Address.AddressResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.AddressRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRoleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Converter {

  private final AddressRepository addressRepository;
  private final UserRoleRepository userRoleRepository;

  public UserResponse mapToUserResponse(User user) {
    return mapResponse(user);
  }

  public UserResponse mapResponse(User user) {
    UserResponse.UserResponseBuilder builder = UserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .bio(user.getBio())
        .avatarUrl(user.getAvatarUrl())
        .status(user.getStatus() != null ? user.getStatus().name() : null)
        .isFirstLogin(user.getIsFirstLogin())
        .trustScore(user.getTrustScore())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .roleNames(userRoleRepository.findRoleNamesByUserId(user.getUserId()));

    Optional<Address> defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(user.getUserId());
    defaultAddress.ifPresent(addr -> {
      builder.addressLine(addr.getAddressLine());
      builder.defaultAddressId(addr.getAddressId());
    });

    return builder.build();
  }

  public AddressResponse mapToAddressResponse(Address address) {
    return AddressResponse.builder()
        .addressId(address.getAddressId())
        .userId(address.getUserId())
        .province(address.getProvince())
        .ward(address.getWard())
        .addressDetail(address.getAddressDetail())
        .addressLine(address.getAddressLine())
        .isDefault(address.getIsDefault())
        .createdAt(address.getCreatedAt())
        .build();
  }
}

package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Address.CreateAddressRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Address.AddressResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.repository.AddressRepository;
import com.quocchung.cntt1.techcycle_system.service.AddressService;
import com.quocchung.cntt1.techcycle_system.utils.Converter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

  private final AddressRepository addressRepository;
  private final Converter converter;

  @Override
  @Transactional
  public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
    List<Address> existingAddresses = addressRepository.findByUserId(userId);
    boolean isFirstAddress = existingAddresses.isEmpty();
    boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault()) || isFirstAddress;

    if (shouldBeDefault) {
      addressRepository.findByUserId(userId).forEach(addr -> {
        addr.setIsDefault(false);
        addressRepository.save(addr);
      });
    }

    Address address = Address.builder()
        .userId(userId)
        .province(request.getProvince().trim())
        .ward(request.getWard() != null ? request.getWard().trim() : null)
        .addressDetail(request.getAddressDetail().trim())
        .addressLine(request.getAddressLine() != null ? request.getAddressLine().trim() : buildAddressLine(request))
        .isDefault(shouldBeDefault)
        .build();

    Address saved = addressRepository.save(address);
    return converter.mapToAddressResponse(saved);
  }

  @Override
  public List<AddressResponse> getAddressesByUserId(Long userId) {
    List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    return addresses.stream()
        .map(converter::mapToAddressResponse)
        .toList();
  }

  @Override
  @Transactional
  public AddressResponse setDefaultAddress(Long userId, Long addressId) {
    Address address = addressRepository.findById(addressId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Address not found"));

    if (!address.getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED, "Address does not belong to this user");
    }

    addressRepository.findByUserId(userId).forEach(addr -> {
      addr.setIsDefault(false);
      addressRepository.save(addr);
    });

    address.setIsDefault(true);
    Address saved = addressRepository.save(address);
    return converter.mapToAddressResponse(saved);
  }

  private String buildAddressLine(CreateAddressRequest request) {
    StringBuilder sb = new StringBuilder();
    if (request.getAddressDetail() != null) {
      sb.append(request.getAddressDetail());
    }
    if (request.getWard() != null && !request.getWard().isBlank()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(request.getWard());
    }
    if (request.getProvince() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(request.getProvince());
    }
    return sb.toString();
  }
}

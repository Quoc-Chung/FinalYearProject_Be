package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Address.CreateAddressRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Address.AddressResponse;
import java.util.List;

public interface AddressService {
  AddressResponse createAddress(Long userId, CreateAddressRequest request);

  List<AddressResponse> getAddressesByUserId(Long userId);

  AddressResponse setDefaultAddress(Long userId, Long addressId);
}

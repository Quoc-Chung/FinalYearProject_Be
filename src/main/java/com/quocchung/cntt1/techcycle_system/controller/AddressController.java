package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Address.CreateAddressRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Address.AddressResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.AddressService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

  private final AddressService addressService;
  private final ResponseUtils responseUtils;
  private final UserRepository userRepository;

  @PostMapping("/add")
  public ResponseEntity<APIResponse<AddressResponse>> createAddress(
      Principal principal,
      @Valid @ModelAttribute CreateAddressRequest request
  ) {
    Long userId = getUserIdFromPrincipal(principal);
    AddressResponse response = addressService.createAddress(userId, request);
    return ResponseEntity.ok(responseUtils.success(response));
  }
  @GetMapping("/user/{userId}")
  public ResponseEntity<APIResponse<AddressResponse>> getAddressesByUserId(
      @PathVariable Long userId
  ) {
    List<AddressResponse> addresses = addressService.getAddressesByUserId(userId);
    return ResponseEntity.ok(responseUtils.successList(addresses));
  }
  @PutMapping("/set-default/{addressId}")
  public ResponseEntity<APIResponse<AddressResponse>> setDefaultAddress(
      Principal principal,
      @PathVariable Long addressId
  ) {
    Long userId = getUserIdFromPrincipal(principal);
    AddressResponse response = addressService.setDefaultAddress(userId, addressId);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  private Long getUserIdFromPrincipal(Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }
    User user = userRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "User not found"));
    return user.getUserId();
  }
}

package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.service.UserService;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.ResponseStatus;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/me")
  public Map<String, Object> me(Principal principal) {
    return Map.of(
        "email", principal != null ? principal.getName() : null
    );
  }

  @GetMapping("/admin-only")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> adminOnly() {
    return Map.of("message", "You are ADMIN");
  }

  @PutMapping(value = "/me", consumes = {"multipart/form-data"})
  public APIResponse<UserResponse> updateMe(
      Principal principal,
      @Valid @ModelAttribute UserRequest request
  ) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new ResException(ResErrorCode.UNAUTHORIZED);
    }

    UserResponse data = userService.updateMe(principal.getName(), request);

    APIResponse<UserResponse> response = new APIResponse<>();
    response.setStatus(new ResponseStatus(
        ResponseStatus.SUCCESS_CODE,
        ResponseStatus.SUCCESS_MESSAGE,
        ResponseStatus.SUCCESS_LABEL
    ));

    response.setData(Collections.singletonList(data));
    response.setPage(null);
    response.setExtraData(Collections.emptyMap());
    return response;
  }
}

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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

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

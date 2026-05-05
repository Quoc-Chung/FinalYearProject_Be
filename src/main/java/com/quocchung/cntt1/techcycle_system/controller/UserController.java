package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UpdateUserStatusRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserSearchRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserSearchResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.service.UserService;
import com.quocchung.cntt1.techcycle_system.utils.PageResponseUtil;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.ResponseStatus;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(Collections.singletonList(data));
    response.setPage(null);
    response.setExtraData(Collections.emptyMap());
    return response;
  }

  @PostMapping("/update-status/{userId}")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ADMIN')")
  public APIResponse<Void> updateUserStatus(
      @PathVariable Long userId,
      @Valid @RequestBody UpdateUserStatusRequest request
  ) {
    userService.updateUserStatus(userId, request);

    APIResponse<Void> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(null);
    response.setPage(null);
    response.setExtraData(Collections.emptyMap());
    return response;
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ADMIN')")
  public APIResponse<UserResponse> searchUsers(
      @RequestParam(required = false) String searchText,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size
  ) {
    UserSearchRequest request = UserSearchRequest.builder()
        .searchText(searchText)
        .status(status)
        .page(page)
        .size(size)
        .build();

    UserSearchResponse searchResult = userService.searchUsers(request);

    PageResponse pageInfo = PageResponseUtil.buildPageMetadata(
        page.longValue(),
        size.longValue(),
        searchResult.getUsers().size(),
        searchResult.getTotalElements() != null ? searchResult.getTotalElements() : 0L
    );

    APIResponse<UserResponse> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(searchResult.getUsers());
    response.setPage(pageInfo);
    response.setExtraData(Collections.emptyMap());
    return response;
  }
}

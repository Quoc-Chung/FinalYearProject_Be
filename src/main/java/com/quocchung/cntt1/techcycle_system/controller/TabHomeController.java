package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.PostUserFollowResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SearchPopularResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SuggestedSellerResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.TodayActivityResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.TabHomeService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tabhome")
@RequiredArgsConstructor
public class TabHomeController {

  private final TabHomeService tabHomeService;
  private final ResponseUtils responseUtils;

  @GetMapping("/posts-following")
  public ResponseEntity<APIResponse<PostUserFollowResponse>> getPostsByFollowedUsers(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<PostUserFollowResponse> result = tabHomeService.getPostsByFollowedUsers(userId);
    return ResponseEntity.ok(responseUtils.successList(result));
  }

  @GetMapping("/popular-searches")
  public ResponseEntity<APIResponse<SearchPopularResponse>> getPopularSearches() {
    List<SearchPopularResponse> result = tabHomeService.getPopularSearches();
    return ResponseEntity.ok(responseUtils.successList(result));
  }

  @GetMapping("/suggested-sellers")
  public ResponseEntity<APIResponse<SuggestedSellerResponse>> getSuggestedSellers(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
    List<SuggestedSellerResponse> result = tabHomeService.getSuggestedSellers(userId);
    return ResponseEntity.ok(responseUtils.successList(result));
  }

  @GetMapping("/today-activity")
  public ResponseEntity<APIResponse<TodayActivityResponse>> getTodayActivity() {
    TodayActivityResponse result = tabHomeService.getTodayActivity();
    return ResponseEntity.ok(responseUtils.success(result));
  }
}

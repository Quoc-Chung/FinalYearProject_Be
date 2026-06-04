package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.SearchHistoryRequest.SearchHistoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.SearchHistoryResponse.SearchHistoryResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.SearchHistoryService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search-history")
public class SearchHistoryController {

  private final SearchHistoryService searchHistoryService;
  private final ResponseUtils responseUtils;

  @PostMapping
  public APIResponse<SearchHistoryResponse> saveSearch(
      @Valid @RequestBody SearchHistoryRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    SearchHistoryResponse response = searchHistoryService
        .saveSearch(request, userPrincipal.getUserId());
    return responseUtils.success(
        response
    );
  }

  @GetMapping("/recent")
  public APIResponse<List<String>> getRecentSearches(
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    List<String> keywords = searchHistoryService
        .getRecentSearches(userPrincipal.getUserId(), limit);
    return responseUtils.success(keywords);
  }

  @DeleteMapping
  public APIResponse<Void> clearSearchHistory(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    searchHistoryService.clearSearchHistory(userPrincipal.getUserId());
    return responseUtils.success(null);
  }
}
package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.SearchHistoryRequest.SearchHistoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.SearchHistoryResponse.SearchHistoryResponse;
import java.util.List;

public interface SearchHistoryService {
  SearchHistoryResponse saveSearch(SearchHistoryRequest request, Long userId);
  List<String> getRecentSearches(Long userId, int limit);
  void clearSearchHistory(Long userId);
}
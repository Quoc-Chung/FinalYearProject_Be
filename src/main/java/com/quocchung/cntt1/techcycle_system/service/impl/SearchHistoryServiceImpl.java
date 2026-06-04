package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.SearchHistoryRequest.SearchHistoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.SearchHistoryResponse.SearchHistoryResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.SearchHistory;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.SearchHistoryRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.SearchHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

  private final SearchHistoryRepository searchHistoryRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public SearchHistoryResponse saveSearch(SearchHistoryRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    SearchHistory history = SearchHistory.builder()
        .user(user)
        .keyword(request.getKeyword().trim())
        .resultCount(request.getResultCount())
        .build();

    SearchHistory saved = searchHistoryRepository.save(history);

    return SearchHistoryResponse.builder()
        .searchId(saved.getSearchId())
        .keyword(saved.getKeyword())
        .resultCount(saved.getResultCount())
        .createdAt(saved.getCreatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getRecentSearches(Long userId, int limit) {
    int safeLimit = Math.min(Math.max(limit, 1), 50);
    return searchHistoryRepository.findRecentKeywordsByUserId(userId, safeLimit);
  }

  @Override
  @Transactional
  public void clearSearchHistory(Long userId) {
    searchHistoryRepository.deleteAllByUserUserId(userId);
  }
}

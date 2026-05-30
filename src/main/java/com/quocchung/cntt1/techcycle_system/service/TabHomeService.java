package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.PostUserFollowResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SearchPopularResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.SuggestedSellerResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.TodayActivityResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.TabHome.NewestPostSidebarResponse;
import java.util.List;

public interface TabHomeService {

  List<PostUserFollowResponse> getPostsByFollowedUsers(Long userId);

  List<SearchPopularResponse> getPopularSearches();

  List<SuggestedSellerResponse> getSuggestedSellers(Long userId);

  TodayActivityResponse getTodayActivity();

  List<NewestPostSidebarResponse> getNewestPostsForSidebar(int limit);
}

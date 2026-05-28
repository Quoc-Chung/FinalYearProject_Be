package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Reaction.ReactionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.PostReactionReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionResponse;
import java.util.List;
import java.util.Map;

public interface ReactionService {

  ReactionResponse addReaction(Long postId, ReactionRequest request, Long userId);

  ReactionResponse updateReaction(Long postId, ReactionRequest request, Long userId);

  void removeReaction(Long postId, Long userId);

  ReactionResponse getReactionsByPost(Long postId, Long currentUserId);

  List<ReactionResponse> getUserReactions(Long userId);

  PostReactionReportResponse getPostReactionReport(Long postId);

  Map<Long, Map<String, Object>> getReactionsCountsForPosts(List<Long> postIds);
}

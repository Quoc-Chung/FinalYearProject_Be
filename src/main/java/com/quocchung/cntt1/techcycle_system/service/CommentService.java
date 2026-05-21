package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Comment.CommentRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Comment.CommentResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;

public interface CommentService {

  CommentResponse createComment(CommentRequest request, Long userId);

  CommentResponse updateComment(Long commentId, CommentRequest request, Long userId);

  void deleteComment(Long commentId, Long userId);

  void hideComment(Long commentId, Long userId);

  void showComment(Long commentId, Long userId);

  void pinComment(Long commentId, Long userId);

  void unpinComment(Long commentId, Long userId);

  void reactToComment(Long commentId, ReactionType reactionType, Long userId);

  void removeReaction(Long commentId, Long userId);

  CommentResponse getCommentById(Long commentId, Long userId);

  List<CommentResponse> getCommentsByPostId(Long postId, Long userId);

  long countCommentsByPostId(Long postId);
}

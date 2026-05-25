package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.Comment.CommentRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Comment.CommentResponse;
import com.quocchung.cntt1.techcycle_system.model.Comment;
import com.quocchung.cntt1.techcycle_system.model.CommentImage;
import com.quocchung.cntt1.techcycle_system.model.CommentReaction;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.CommentImageRepository;
import com.quocchung.cntt1.techcycle_system.repository.CommentReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.CommentRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.CommentService;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final CommentImageRepository commentImageRepository;
  private final CommentReactionRepository commentReactionRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final MinioProperties minioProperties;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public CommentResponse createComment(CommentRequest request, Long userId) {
    Post post = postRepository.findById(request.getPostId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

    Comment parentComment = null;
    if (request.getCommentParentId() != null) {
      parentComment = commentRepository.findById(request.getCommentParentId())
          .orElseThrow(() -> new RuntimeException("Không tìm thấy comment cha"));
    }

    Comment comment = Comment.builder()
        .post(post)
        .user(user)
        .parent(parentComment)
        .content(request.getContent() != null ? request.getContent() : "")
        .build();

    comment = commentRepository.save(comment);

    if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
      for (CommentRequest.MediaEntry media : request.getMediaList()) {
        CommentImage image = CommentImage.builder()
            .comment(comment)
            .bucket("techcycle")
            .objectKey(media.getObjectKey())
            .fileName(media.getFileName())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .width(media.getWidth())
            .height(media.getHeight())
            .build();
        commentImageRepository.save(image);
      }
    }

    if (!post.getUser().getUserId().equals(userId)) {
      String preview = request.getContent() != null && !request.getContent().isBlank()
          ? (request.getContent().length() > 50
              ? request.getContent().substring(0, 50) + "..."
              : request.getContent())
          : "Đã bình luận trên bài viết của bạn";
      notificationService.createNotification(
          post.getUser(),
          user,
          NotificationType.POST_COMMENTED,
          user.getFullName() + " đã bình luận về bài viết của bạn",
          preview,
          "/post/" + post.getPostId(),
          Map.of("postId", post.getPostId(), "commentId", comment.getCommentId())
      );
    }

    return buildCommentResponse(comment, userId);
  }

  @Override
  @Transactional
  public CommentResponse updateComment(Long commentId, CommentRequest request, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền chỉnh sửa comment này");
    }

    if (request.getContent() != null) {
      comment.setContent(request.getContent());
    }

    if (request.getMediaList() != null) {
      commentImageRepository.deleteByCommentCommentId(commentId);

      for (CommentRequest.MediaEntry media : request.getMediaList()) {
        CommentImage image = CommentImage.builder()
            .comment(comment)
            .bucket("techcycle")
            .objectKey(media.getObjectKey())
            .fileName(media.getFileName())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .width(media.getWidth())
            .height(media.getHeight())
            .build();
        commentImageRepository.save(image);
      }
    }

    comment = commentRepository.save(comment);
    return buildCommentResponse(comment, userId);
  }

  @Override
  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền xóa comment này");
    }

    commentImageRepository.deleteByCommentCommentId(commentId);
    commentReactionRepository.deleteByCommentCommentId(commentId);
    commentRepository.delete(comment);
  }

  @Override
  @Transactional
  public void hideComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền ẩn comment này");
    }

    comment.setIsHidden(true);
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void showComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền hiển thị comment này");
    }

    comment.setIsHidden(false);
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void pinComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền ghim comment này");
    }

    comment.setIsPinned(true);
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void unpinComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    if (!comment.getUser().getUserId().equals(userId)) {
      throw new RuntimeException("Bạn không có quyền bỏ ghim comment này");
    }

    comment.setIsPinned(false);
    commentRepository.save(comment);
  }

  @Override
  @Transactional
  public void reactToComment(Long commentId, ReactionType reactionType, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

    Optional<CommentReaction> existingReaction =
        commentReactionRepository.findByCommentCommentIdAndUserUserId(commentId, userId);

    if (existingReaction.isPresent()) {
      CommentReaction reaction = existingReaction.get();
      reaction.setReactionType(reactionType);
      commentReactionRepository.save(reaction);
    } else {
      CommentReaction reaction = CommentReaction.builder()
          .comment(comment)
          .user(user)
          .reactionType(reactionType)
          .build();
      commentReactionRepository.save(reaction);
      commentRepository.incrementLikesCount(commentId);

      if (!comment.getUser().getUserId().equals(userId)) {
        notificationService.createNotification(
            comment.getUser(),
            user,
            NotificationType.COMMENT_REACTED,
            user.getFullName() + " đã bày tỏ cảm xúc về bình luận của bạn",
            user.getFullName() + " đã " + getReactionText(reactionType) + " bình luận của bạn",
            "/post/" + comment.getPost().getPostId(),
            Map.of("postId", comment.getPost().getPostId(), "commentId", commentId)
        );
      }
    }
  }

  private String getReactionText(ReactionType type) {
    return switch (type) {
      case LIKE -> "thích";
      case LOVE -> "yêu thích";
      case HAHA -> "haha";
      case WOW -> "wow";
      case SAD -> "buồn";
      case ANGRY -> "giận";
    };
  }

  @Override
  @Transactional
  public void removeReaction(Long commentId, Long userId) {
    commentReactionRepository.deleteByCommentCommentIdAndUserUserId(commentId, userId);
    commentRepository.decrementLikesCount(commentId);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentResponse getCommentById(Long commentId, Long userId) {
    Comment comment = commentRepository.findByIdWithUser(commentId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));
    return buildCommentResponse(comment, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsByPostId(Long postId, Long userId) {
    List<Comment> topLevelComments =
        commentRepository.findByPostPostIdAndParentIsNullOrderByIsPinnedDescCreatedAtDesc(postId);

    return topLevelComments.stream()
        .map(comment -> buildCommentResponse(comment, userId))
        .collect(Collectors.toList());
  }

  private String buildPublicUrl(String objectKey) {
    String base = minioProperties.getPublicEndpoint();
    if (base == null || base.isBlank()) {
      base = minioProperties.getEndpoint();
    }
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/" + minioProperties.getBucketName() + "/" + objectKey;
  }

  @Override
  @Transactional(readOnly = true)
  public long countCommentsByPostId(Long postId) {
    return commentRepository.countByPostId(postId);
  }

  private CommentResponse buildCommentResponse(Comment comment, Long currentUserId) {
    String userReaction = null;
    if (currentUserId != null) {
      Optional<CommentReaction> userReactOpt =
          commentReactionRepository.findByCommentCommentIdAndUserUserId(
              comment.getCommentId(), currentUserId);
      if (userReactOpt.isPresent()) {
        userReaction = userReactOpt.get().getReactionType().name();
      }
    }

    long reactionCount = commentReactionRepository.countByCommentCommentId(comment.getCommentId());

    List<CommentImage> images =
        commentImageRepository.findByCommentCommentIdOrderByCreatedAtAsc(comment.getCommentId());

    List<CommentResponse.MediaInfo> mediaInfoList = images.stream()
        .map(img -> CommentResponse.MediaInfo.builder()
            .imageId(img.getImageId())
            .mediaType(determineMediaType(img.getMimeType()))
            .publicUrl( buildPublicUrl(img.getObjectKey()))
            .thumbnailUrl(img.getMimeType() != null && img.getMimeType().startsWith("video/")
                ? generateThumbnailUrl(img.getObjectKey()) : null)
            .width(img.getWidth())
            .height(img.getHeight())
            .build())
        .collect(Collectors.toList());

    List<Comment> children =
        commentRepository.findByPostPostIdAndParentCommentIdOrderByCreatedAtAsc(
            comment.getPost().getPostId(), comment.getCommentId());

    List<CommentResponse> childrenResponses = children.stream()
        .map(child -> buildCommentResponse(child, currentUserId))
        .collect(Collectors.toList());

    return CommentResponse.builder()
        .commentId(comment.getCommentId())
        .userNameComment(comment.getUser().getFullName())
        .userAvatarUrl(comment.getUser().getAvatarUrl())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .countReactionComent(reactionCount)
        .userReactionComment(userReaction)
        .isPinned(comment.getIsPinned())
        .isHidden(comment.getIsHidden())
        .mediaInfoList(mediaInfoList.isEmpty() ? null : mediaInfoList)
        .commentChildrenResponse(childrenResponses.isEmpty() ? null : childrenResponses)
        .build();
  }

  private MediaType determineMediaType(String mimeType) {
    if (mimeType == null) {
      return MediaType.IMAGE;
    }
    return mimeType.toLowerCase().startsWith("video/") ? MediaType.VIDEO : MediaType.IMAGE;
  }
  private String generateThumbnailUrl(String objectKey) {
    return objectKey.replace("/video/", "/thumbnail/video/")
        .replace(".mp4", "_thumb.jpg");
  }


}

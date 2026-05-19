package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Reaction.ReactionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.PostReactionReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Reaction.ReactionUserInfo;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostReaction;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.AddressRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.ReactionService;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

  private final PostReactionRepository postReactionRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final AddressRepository addressRepository;

  @Override
  @Transactional
  public ReactionResponse addReaction(Long postId, ReactionRequest request, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Optional<PostReaction> existingReaction = postReactionRepository
        .findByPostPostIdAndUserUserId(postId, userId);

    if (existingReaction.isPresent()) {
      throw new ResException(ResErrorCode.POST_REACTION_EXISTED);
    }

    PostReaction reaction = PostReaction.builder()
        .post(post)
        .user(user)
        .reactionType(request.getReactionType())
        .build();

    PostReaction saved = postReactionRepository.save(reaction);

    return mapToResponse(saved, postId, currentUserId -> {
      Optional<PostReaction> userReaction = postReactionRepository
          .findByPostPostIdAndUserUserId(postId, currentUserId);
      return userReaction.map(PostReaction::getReactionType).orElse(null);
    }, userId);
  }

  @Override
  @Transactional
  public ReactionResponse updateReaction(Long postId, ReactionRequest request, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    PostReaction reaction = postReactionRepository.findByPostPostIdAndUserUserId(postId, userId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_REACTION_NOT_FOUND));

    reaction.setReactionType(request.getReactionType());
    PostReaction saved = postReactionRepository.save(reaction);

    return mapToResponse(saved, postId, currentUserId -> {
      Optional<PostReaction> userReaction = postReactionRepository
          .findByPostPostIdAndUserUserId(postId, currentUserId);
      return userReaction.map(PostReaction::getReactionType).orElse(null);
    }, userId);
  }

  @Override
  @Transactional
  public void removeReaction(Long postId, Long userId) {
    if (!postReactionRepository.existsByPostPostIdAndUserUserId(postId, userId)) {
      throw new ResException(ResErrorCode.POST_REACTION_NOT_FOUND);
    }

    postReactionRepository.deleteByPostPostIdAndUserUserId(postId, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public ReactionResponse getReactionsByPost(Long postId, Long currentUserId) {
    if (!postRepository.existsById(postId)) {
      throw new ResException(ResErrorCode.POST_NOT_FOUND);
    }

    Map<ReactionType, Long> reactionCounts = getReactionCounts(postId);
    long totalReactions = reactionCounts.values().stream().mapToLong(Long::longValue).sum();

    ReactionType userReactionType = null;
    if (currentUserId != null) {
      Optional<PostReaction> userReaction = postReactionRepository
          .findByPostPostIdAndUserUserId(postId, currentUserId);
      userReactionType = userReaction.map(PostReaction::getReactionType).orElse(null);
    }

    return ReactionResponse.builder()
        .postId(postId)
        .reactionCounts(reactionCounts)
        .totalReactions(totalReactions)
        .userReactionType(userReactionType)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReactionResponse> getUserReactions(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResException(ResErrorCode.USER_NOT_FOUND);
    }

    List<PostReaction> reactions = postReactionRepository.findAll().stream()
        .filter(r -> r.getUser().getUserId().equals(userId))
        .toList();

    return reactions.stream()
        .map(r -> mapToResponse(r, r.getPost().getPostId(), currentUserId -> {
          Optional<PostReaction> userReaction = postReactionRepository
              .findByPostPostIdAndUserUserId(r.getPost().getPostId(), currentUserId);
          return userReaction.map(PostReaction::getReactionType).orElse(null);
        }, userId))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PostReactionReportResponse getPostReactionReport(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    Map<ReactionType, Long> reactionCounts = getReactionCounts(postId);
    long totalReactions = reactionCounts.values().stream().mapToLong(Long::longValue).sum();

    ReactionType topReaction = reactionCounts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .filter(e -> e.getValue() > 0)
        .map(Map.Entry::getKey)
        .orElse(null);

    List<PostReaction> allReactions = postReactionRepository.findByPostPostId(postId);

    return PostReactionReportResponse.builder()
        .postId(postId)
        .totalReactions(totalReactions)
        .reactionCounts(reactionCounts)
        .topReaction(topReaction)
        .likeCount(reactionCounts.getOrDefault(ReactionType.LIKE, 0L))
        .loveCount(reactionCounts.getOrDefault(ReactionType.LOVE, 0L))
        .hahaCount(reactionCounts.getOrDefault(ReactionType.HAHA, 0L))
        .wowCount(reactionCounts.getOrDefault(ReactionType.WOW, 0L))
        .sadCount(reactionCounts.getOrDefault(ReactionType.SAD, 0L))
        .angryCount(reactionCounts.getOrDefault(ReactionType.ANGRY, 0L))
        .likeUsers(getReactionUsers(allReactions, ReactionType.LIKE))
        .loveUsers(getReactionUsers(allReactions, ReactionType.LOVE))
        .hahaUsers(getReactionUsers(allReactions, ReactionType.HAHA))
        .wowUsers(getReactionUsers(allReactions, ReactionType.WOW))
        .sadUsers(getReactionUsers(allReactions, ReactionType.SAD))
        .angryUsers(getReactionUsers(allReactions, ReactionType.ANGRY))
        .build();
  }

  private List<ReactionUserInfo> getReactionUsers(List<PostReaction> reactions, ReactionType type) {
    return reactions.stream()
        .filter(r -> r.getReactionType() == type)
        .map(r -> {
          User user = r.getUser();
          String ward = null;
          String province = null;
          Optional<Address> defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(user.getUserId());
          if (defaultAddress.isPresent()) {
            ward = defaultAddress.get().getWard();
            province = defaultAddress.get().getProvince();
          }
          return ReactionUserInfo.builder()
              .userId(user.getUserId())
              .fullName(user.getFullName())
              .ward(ward)
              .province(province)
              .avatarUrl(user.getAvatarUrl())
              .trustScore(user.getTrustScore())
              .build();
        })
        .toList();
  }

  private Map<ReactionType, Long> getReactionCounts(Long postId) {
    List<Object[]> results = postReactionRepository.countReactionsByType(postId);
    Map<ReactionType, Long> counts = new HashMap<>();

    for (ReactionType type : ReactionType.values()) {
      counts.put(type, 0L);
    }

    for (Object[] row : results) {
      ReactionType type = (ReactionType) row[0];
      Long count = (Long) row[1];
      counts.put(type, count);
    }

    return counts;
  }

  private ReactionResponse mapToResponse(
      PostReaction reaction,
      Long postId,
      java.util.function.Function<Long, ReactionType> userReactionResolver,
      Long currentUserId
  ) {
    Map<ReactionType, Long> reactionCounts = getReactionCounts(postId);
    long totalReactions = reactionCounts.values().stream().mapToLong(Long::longValue).sum();

    return ReactionResponse.builder()
        .reactionId(reaction.getReactionId())
        .postId(reaction.getPost().getPostId())
        .userId(reaction.getUser().getUserId())
        .reactionType(reaction.getReactionType())
        .createdAt(reaction.getCreatedAt())
        .reactionCounts(reactionCounts)
        .totalReactions(totalReactions)
        .userReactionType(userReactionResolver.apply(currentUserId))
        .build();
  }
}

package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UpdateUserStatusRequest;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserSearchRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserMetadataResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserSearchResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Seller.SellerProfileResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.TrustScoreResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.mapper.UserMapper;
import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserImage;
import com.quocchung.cntt1.techcycle_system.repository.AddressRepository;
import com.quocchung.cntt1.techcycle_system.repository.ConversationRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReportRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserFollowRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserImageRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserReviewRepository;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.service.UserService;
import com.quocchung.cntt1.techcycle_system.utils.Converter;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.UserStatus;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserImageRepository userImageRepository;
  private final AddressRepository addressRepository;
  private final MinIoService minioService;
  private final Converter converter;
  private final UserMapper userMapper;
  private final PostRepository postRepository;
  private final UserFollowRepository userFollowRepository;
  private final UserReviewRepository userReviewRepository;
  private final ConversationRepository conversationRepository;
  private final PostReportRepository postReportRepository;
  private final MinioProperties minioProperties;

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
  @Transactional
  public UserResponse updateMe(String email, UserRequest request) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "User not found"));

    updateBasicInfo(user, request);
    handleAvatar(user, request.getAvatar());
    handleAddressId(user, request.getAddressId());

    User saved = userRepository.save(user);
    return converter.mapResponse(saved);
  }

  private void updateBasicInfo(User user, UserRequest request) {
    if (request.getFullName() != null && !request.getFullName().isBlank()) {
      user.setFullName(request.getFullName().trim());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone().trim());
    }
    if (request.getBio() != null) {
      user.setBio(request.getBio().trim());
    }
  }

  private void handleAvatar(User user, MultipartFile avatarFile) {
    if (avatarFile == null || avatarFile.isEmpty()) {
      return;
    }

    UserImage currentAvatar = userImageRepository.findFirstByUserUserIdAndIsAvatarTrue(
        user.getUserId()).orElse(null);
    String oldObjectKey = currentAvatar != null ? currentAvatar.getObjectKey() : null;

    StorageUploadResponse uploadResult = minioService.uploadUserAvatar(avatarFile,
        user.getUserId());
    user.setAvatarUrl(uploadResult.getUrl());

    UserImage avatarRow = currentAvatar != null ? currentAvatar : UserImage.builder()
        .user(user)
        .isAvatar(true)
        .build();

    avatarRow.setObjectKey(uploadResult.getObjectKey());
    avatarRow.setImageUrl(uploadResult.getUrl());
    userImageRepository.save(avatarRow);

    if (oldObjectKey != null && !oldObjectKey.equals(uploadResult.getObjectKey())) {
      minioService.deleteObjectSilently(oldObjectKey);
    }
  }

  private void handleAddressId(User user, Long addressId) {
    if (addressId == null) {
      return;
    }

    Address address = addressRepository.findById(addressId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Address not found"));

    if (!address.getUserId().equals(user.getUserId())) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED, "Address does not belong to this user");
    }

    List<Address> userAddresses = addressRepository.findByUserId(user.getUserId());
    userAddresses.forEach(addr -> {
      addr.setIsDefault(false);
      addressRepository.save(addr);
    });

    address.setIsDefault(true);
    addressRepository.save(address);
  }

  @Override
  @Transactional
  public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "User not found"));

    UserStatus newStatus;
    try {
      newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ResException(ResErrorCode.INVALID_REQUEST, "Invalid status value: " + request.getStatus());
    }

    user.setStatus(newStatus);

    if (request.getReason() != null && !request.getReason().isBlank()) {
      user.setBannedReason(request.getReason().trim());
    } else {
      user.setBannedReason(null);
    }

    userRepository.save(user);
  }

  @Override
  public UserSearchResponse searchUsers(UserSearchRequest request) {
    int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
    int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
    int offset = (page - 1) * size;

    List<UserResponse> users = userMapper.searchUsers(
        request.getSearchText(),
        request.getStatus(),
        offset,
        size
    );
    Long totalElements = userMapper.countSearchUsers(
        request.getSearchText(),
        request.getStatus()
    );
    return UserSearchResponse.builder()
        .users(users)
        .totalElements(totalElements)
        .build();
  }

  @Override
  public UserMetadataResponse getUserMetadata(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));

    long countPost = postRepository.countByUserUserId(userId);
    long countUserFollow = userFollowRepository.countFollowersByUserId(userId);
    Double ratingScore = userReviewRepository.getAverageRatingByUserId(userId);
    Double userTrustScore = user.getTrustScore();
    Long trustScore = userTrustScore != null ? userTrustScore.longValue() : 0L;

    return UserMetadataResponse.builder()
        .trustScore(trustScore)
        .countPost(countPost)
        .countUserFollow(countUserFollow)
        .ratingScore(ratingScore != null ? ratingScore : 0.0)
        .build();
  }

  @Override
  public double calculateTrustScore(Long userId) {
    double score = 50.0;

    Double avgRating = userReviewRepository.findAverageRating(userId);
    long reviewCount = userReviewRepository.countByToUserId(userId);

    double ratingBonus = 0.0;
    if (avgRating != null && reviewCount >= 3) {
      ratingBonus = (avgRating - 3.0) / 2.0 * 30;
      score += ratingBonus;
    } else if (avgRating != null && reviewCount > 0) {
      ratingBonus = (avgRating - 3.0) / 2.0 * 30 * 0.5;
      score += ratingBonus;
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));

    double tenureBonus = 0.0;
    if (user.getCreatedAt() != null) {
      long monthsActive = ChronoUnit.MONTHS.between(user.getCreatedAt(), LocalDateTime.now());
      if (monthsActive >= 12) {
        tenureBonus = 10.0;
        score += 10;
      } else if (monthsActive >= 6) {
        tenureBonus = 5.0;
        score += 5;
      }
    }

    long violations = postReportRepository.countViolationReportsByUserId(userId);
    double violationPenalty = violations * 10;
    score -= violationPenalty;

    return Math.max(0, Math.min(100, Math.round(score * 10.0) / 10.0));
  }

  @Override
  @Transactional(readOnly = true)
  public TrustScoreResponse getTrustScore(Long userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));

    Double avgRating = userReviewRepository.findAverageRating(userId);
    int reviewCount = (int) userReviewRepository.countByToUserId(userId);
    long followerCount = userFollowRepository.countFollowersByUserId(userId);
    long violationCount = postReportRepository.countViolationReportsByUserId(userId);

    double ratingBonus = 0.0;
    if (avgRating != null && reviewCount >= 3) {
      ratingBonus = (avgRating - 3.0) / 2.0 * 30;
    } else if (avgRating != null && reviewCount > 0) {
      ratingBonus = (avgRating - 3.0) / 2.0 * 30 * 0.5;
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));

    double tenureBonus = 0.0;
    if (user.getCreatedAt() != null) {
      long monthsActive = ChronoUnit.MONTHS.between(user.getCreatedAt(), LocalDateTime.now());
      if (monthsActive >= 12) {
        tenureBonus = 10.0;
      } else if (monthsActive >= 6) {
        tenureBonus = 5.0;
      }
    }

    double violationPenalty = violationCount * 10;
    double trustScore = calculateTrustScore(userId);

    return TrustScoreResponse.from(
        userId,
        trustScore,
        avgRating,
        reviewCount,
        followerCount,
        violationCount,
        ratingBonus,
        tenureBonus,
        violationPenalty
    );
  }

  @Override
  @Transactional(readOnly = true)
  public SellerProfileResponse getSellerProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
            "Không tìm thấy người dùng với id: " + userId));

    String username = user.getFullName();
    String joinDate = "Tham gia từ " + (user.getCreatedAt() != null
        ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy"))
        : "N/A");

    Double avgRating = userReviewRepository.getAverageRatingByUserId(userId);
    long reviewCount = userReviewRepository.countByToUserId(userId);
    String trustScore = (avgRating != null ? String.format("%.1f", avgRating) : "0.0")
        + " • " + (reviewCount > 0 ? reviewCount + " đánh giá" : "Chưa có đánh giá");

    long exchangeCount = conversationRepository.countByParticipantUserId(userId);
    long postCount = postRepository.countByUserUserId(userId);
    SellerProfileResponse.SellerStats stats = SellerProfileResponse.SellerStats.builder()
        .exchanges((int) exchangeCount)
        .posts((int) postCount)
        .responseRate("98%")
        .responseTime("< 10 phút")
        .build();

    List<Post> activePosts = postRepository.findByUserUserIdAndStatus(userId, PostStatus.APPROVED);
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    List<SellerProfileResponse.SellerListing> listings = activePosts.stream()
        .limit(10)
        .map(post -> {
          String formattedPrice = currencyFormat.format(post.getPrice()).replace("₫", "") + "₫";
          String postedAt = formatRelativeTime(post.getCreatedAt());

          String imageUrl = null;
          if (post.getImages() != null && !post.getImages().isEmpty()) {
            PostImage firstImage = post.getImages().stream().findFirst().orElse(null);
            if (firstImage != null && firstImage.getObjectKey() != null) {
              imageUrl = firstImage.getObjectKey();
            }
          }

          return SellerProfileResponse.SellerListing.builder()
              .postId(post.getPostId())
              .title(post.getTitle())
              .price(post.getPrice())
              .formattedPrice(formattedPrice)
              .postedAt(postedAt)
              .image(buildPublicUrl(imageUrl))
              .build();
        })
        .collect(Collectors.toList());

    Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(userId).orElse(null);
    SellerProfileResponse.MeetLocation meetLocation = null;
    if (defaultAddress != null) {
      meetLocation = SellerProfileResponse.MeetLocation.builder()
          .name(defaultAddress.getProvince())
          .address(defaultAddress.getAddressLine())
          .build();
    }

    List<UserImage> userImages = userImageRepository.findAllByUserId(userId);
    List<String> sharedImages = userImages.stream()
        .map(UserImage::getImageUrl)
        .collect(Collectors.toList());

    List<String> notes = Arrays.asList(
        "Ưu tiên gặp trực tiếp để kiểm tra sản phẩm.",
        "Có thể hỗ trợ ship nội thành.",
        "Thường phản hồi rất nhanh.",
        "Cho test kỹ trước khi nhận hàng."
    );

    return SellerProfileResponse.builder()
        .userId(userId)
        .username(username)
        .joinDate(joinDate)
        .trustScore(trustScore)
        .stats(stats)
        .listings(listings)
        .meetLocation(meetLocation)
        .sharedImages(sharedImages)
        .notes(notes)
        .build();
  }

  private String formatRelativeTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "Không rõ";
    }
    LocalDateTime now = LocalDateTime.now();
    long minutes = java.time.Duration.between(dateTime, now).toMinutes();
    long hours = java.time.Duration.between(dateTime, now).toHours();
    long days = java.time.Duration.between(dateTime, now).toDays();

    if (minutes < 60) {
      return minutes + " phút trước";
    } else if (hours < 24) {
      return hours + " giờ trước";
    } else if (days == 1) {
      return "Hôm qua";
    } else if (days < 7) {
      return days + " ngày trước";
    } else if (days < 30) {
      return (days / 7) + " tuần trước";
    } else if (days < 365) {
      return (days / 30) + " tháng trước";
    } else {
      return (days / 365) + " năm trước";
    }
  }
}

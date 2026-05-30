package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.Post.CreatePostRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostDetailUser;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.model.Brand;
import com.quocchung.cntt1.techcycle_system.model.Category;
import com.quocchung.cntt1.techcycle_system.model.Comment;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostAttribute;
import com.quocchung.cntt1.techcycle_system.model.PostImage;
import com.quocchung.cntt1.techcycle_system.model.PostTag;
import com.quocchung.cntt1.techcycle_system.model.PostReaction;
import com.quocchung.cntt1.techcycle_system.model.Tag;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.AddressRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.BrandRepository;
import com.quocchung.cntt1.techcycle_system.repository.CategoryRepository;
import com.quocchung.cntt1.techcycle_system.repository.CommentRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostAttributeRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostImageRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostTagRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostSpecifications;
import com.quocchung.cntt1.techcycle_system.repository.TagRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserFollowRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserReviewRepository;
import com.quocchung.cntt1.techcycle_system.service.PostService;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;
  private final AddressRepository addressRepository;
  private final PostRepository postRepository;
  private final PostImageRepository postImageRepository;
  private final PostAttributeRepository postAttributeRepository;
  private final TagRepository tagRepository;
  private final PostTagRepository postTagRepository;
  private final PostReactionRepository postReactionRepository;
  private final MinioProperties minioProperties;
  private final UserFollowRepository userFollowRepository;
  private final UserReviewRepository userReviewRepository;
  private final CommentRepository commentRepository;
  private final NotificationService notificationService;

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
  public PostResponse createPost(CreatePostRequest request, Long userId) {
    User author = userRepository.findById(userId)
        .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResException(ResErrorCode.CATEGORY_NOT_FOUND));

    Brand brand = null;
    if (request.getBrandId() != null) {
      brand = brandRepository.findById(request.getBrandId())
          .orElseThrow(() -> new ResException(ResErrorCode.BRAND_NOT_FOUND));
    }

    Address address = null;
    if (request.getAddressId() != null) {
      address = addressRepository.findById(request.getAddressId())
          .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));
    }

    Post post = Post.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .price(request.getPrice())
        .originalPrice(request.getOriginalPrice())
        .conditionGrade(request.getConditionGrade())
        .status(PostStatus.PENDING)
        .category(category)
        .brand(brand)
        .address(address)
        .user(author)
        .build();

    postRepository.save(post);

    if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
      List<PostImage> images = new ArrayList<>();
      for (CreatePostRequest.MediaEntry media : request.getMediaList()) {
        MediaType mediaType = media.getMimeType() != null
            && media.getMimeType().toLowerCase().startsWith("video/")
            ? MediaType.VIDEO : MediaType.IMAGE;

        PostImage image = PostImage.builder()
            .post(post)
            .mediaType(mediaType)
            .bucket(minioProperties.getBucketName())
            .objectKey(media.getObjectKey())
            .thumbnailKey(media.getThumbnailKey())
            .fileName(media.getFileName())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .width(media.getWidth())
            .height(media.getHeight())
            .duration(media.getDuration())
            .sortOrder(media.getSortOrder())
            .build();
        images.add(image);
      }
      postImageRepository.saveAll(images);
      post.getImages().addAll(images);
    }

    if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
      List<PostAttribute> attributes = request.getAttributes().stream()
          .filter(a -> a.getAttributeKey() != null && !a.getAttributeKey().isBlank())
          .map(a -> PostAttribute.builder()
              .post(post)
              .attributeKey(a.getAttributeKey())
              .attributeValue(a.getAttributeValue())
              .sortOrder(a.getSortOrder())
              .build())
          .toList();
      postAttributeRepository.saveAll(attributes);
      post.getAttributes().addAll(attributes);
    }

    if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
      List<PostTag> postTags = new ArrayList<>();
      for (String tagName : request.getTagNames()) {
        String slug = slugify(tagName);
        Tag tag = tagRepository.findBySlug(slug)
            .orElseGet(() -> tagRepository.save(
                Tag.builder()
                    .name(tagName)
                    .slug(slug)
                    .isActive(true)
                    .build()));
        postTags.add(PostTag.builder()
            .post(post)
            .tag(tag)
            .build());
      }
        postTagRepository.saveAll(postTags);
        post.getPostTags().addAll(postTags);
      }

      // Gửi thông báo cho tất cả admin khi có bài viết mới
      List<User> admins = userRepository.getAllAdmin();
      for (User admin : admins) {
        String title = "Bài viết mới cần duyệt";
        String content = author.getFullName() + " đã tạo bài viết \"" + post.getTitle() + "\" cần được duyệt";
        String targetUrl = "/admin/posts/pending";
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("postId", post.getPostId());
        data.put("postTitle", post.getTitle());
        data.put("authorName", author.getFullName());

        notificationService.createNotification(
            admin,
            author,
            NotificationType.POST_PENDING,
            title,
            content,
            targetUrl,
            data
        );
      }

      return mapToResponse(post);
    }

    @Override
    public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));
    return mapToResponse(post);
  }

  @Override
  @Transactional
  public PostResponse updatePost(Long id, CreatePostRequest request, Long userId) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    post.setStatus(PostStatus.PENDING);

    if (request.getTitle() != null) {
      post.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      post.setDescription(request.getDescription());
    }
    if (request.getPrice() != null) {
      post.setPrice(request.getPrice());
    }
    if (request.getOriginalPrice() != null) {
      post.setOriginalPrice(request.getOriginalPrice());
    }
    if (request.getConditionGrade() != null) {
      post.setConditionGrade(request.getConditionGrade());
    }

    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new ResException(ResErrorCode.CATEGORY_NOT_FOUND));
      post.setCategory(category);
    }
    if (request.getBrandId() != null) {
      Brand brand = brandRepository.findById(request.getBrandId())
          .orElseThrow(() -> new ResException(ResErrorCode.BRAND_NOT_FOUND));
      post.setBrand(brand);
    }
    if (request.getAddressId() != null) {
      Address address = addressRepository.findById(request.getAddressId())
          .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));
      post.setAddress(address);
    }

    postRepository.save(post);


    post.getImages().clear();
    post.getAttributes().clear();
    post.getPostTags().clear();

    if (request.getMediaList() != null && !request.getMediaList().isEmpty()) {
      List<PostImage> images = new ArrayList<>();
      for (CreatePostRequest.MediaEntry media : request.getMediaList()) {
        MediaType mediaType = media.getMimeType() != null
            && media.getMimeType().toLowerCase().startsWith("video/")
            ? MediaType.VIDEO : MediaType.IMAGE;

        PostImage image = PostImage.builder()
            .post(post)
            .mediaType(mediaType)
            .bucket(minioProperties.getBucketName())
            .objectKey(media.getObjectKey())
            .thumbnailKey(media.getThumbnailKey())
            .fileName(media.getFileName())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .width(media.getWidth())
            .height(media.getHeight())
            .duration(media.getDuration())
            .sortOrder(media.getSortOrder())
            .build();
        images.add(image);
      }
      postImageRepository.saveAll(images);
      post.getImages().addAll(images);
    }

    if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
      List<PostAttribute> attributes = request.getAttributes().stream()
          .filter(a -> a.getAttributeKey() != null && !a.getAttributeKey().isBlank())
          .map(a -> PostAttribute.builder()
              .post(post)
              .attributeKey(a.getAttributeKey())
              .attributeValue(a.getAttributeValue())
              .sortOrder(a.getSortOrder())
              .build())
          .toList();
      postAttributeRepository.saveAll(attributes);
      post.getAttributes().addAll(attributes);
    }

    if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
      // Xóa các tag cũ trong database trước khi thêm mới
      postTagRepository.deleteAll(post.getPostTags());
      post.getPostTags().clear();
      postRepository.saveAndFlush(post);

      List<PostTag> postTags = new ArrayList<>();
      for (String tagName : request.getTagNames()) {
        String slug = slugify(tagName);
        Tag tag = tagRepository.findBySlug(slug)
            .orElseGet(() -> tagRepository.save(
                Tag.builder()
                    .name(tagName)
                    .slug(slug)
                    .isActive(true)
                    .build()));
        postTags.add(PostTag.builder()
            .post(post)
            .tag(tag)
            .build());
      }
      postTagRepository.saveAll(postTags);
      post.getPostTags().addAll(postTags);
    } else {
      // Nếu không có tag mới, xóa hết tag cũ
      postTagRepository.deleteAll(post.getPostTags());
      post.getPostTags().clear();
    }

    return mapToResponse(post);
  }

  @Override
  @Transactional
  public void deletePost(Long id, Long userId) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    postRepository.delete(post);
  }

  @Override
  public Page<PostResponse> getPostsPage(Pageable pageable) {
    return postRepository.findAll(pageable).map(this::mapToResponse);
  }

  @Override
  public Page<PostResponse> getMyPosts(Long userId, Pageable pageable) {
    return postRepository.findByUserUserId(userId, pageable).map(this::mapToResponse);
  }

  @Override
  public List<PostResponse> getApprovedPosts(String keyword, Long categoryId, Long brandId, String ward, String province) {
    Specification<Post> spec = Specification.where(PostSpecifications.hasStatus(PostStatus.APPROVED));

    if (keyword != null && !keyword.isBlank()) {
      spec = spec.and(PostSpecifications.hasKeyword(keyword));
    }
    if (categoryId != null) {
      spec = spec.and(PostSpecifications.hasCategoryId(categoryId));
    }
    if (brandId != null) {
      spec = spec.and(PostSpecifications.hasBrandId(brandId));
    }
    if (ward != null && !ward.isBlank()) {
      spec = spec.and(PostSpecifications.hasWard(ward));
    }
    if (province != null && !province.isBlank()) {
      spec = spec.and(PostSpecifications.hasProvince(province));
    }

    return postRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public Page<PostResponse> searchPosts(
      String keyword,
      String title,
      String description,
      String authorName,
      String address,
      String province,
      String ward,
      Long categoryId,
      Long brandId,
      Double minPrice,
      Double maxPrice,
      String tag,
      Pageable pageable
  ) {
    Specification<Post> spec = Specification.where(null);

    if (keyword != null && !keyword.isBlank()) {
      Specification<Post> keywordSpec = Specification.where(
          PostSpecifications.hasTitle(keyword)
      ).or(PostSpecifications.hasDescription(keyword))
       .or(PostSpecifications.hasAuthorFullName(keyword))
       .or(PostSpecifications.hasAddressLine(keyword));
      spec = spec.and(keywordSpec);
    }

    if (title != null && !title.isBlank()) {
      spec = spec.and(PostSpecifications.hasTitle(title));
    }

    if (description != null && !description.isBlank()) {
      spec = spec.and(PostSpecifications.hasDescription(description));
    }

    if (authorName != null && !authorName.isBlank()) {
      spec = spec.and(PostSpecifications.hasAuthorFullName(authorName));
    }

    if (address != null && !address.isBlank()) {
      spec = spec.and(PostSpecifications.hasAddressLine(address));
    }

    if (province != null && !province.isBlank()) {
      spec = spec.and(PostSpecifications.hasProvince(province));
    }

    if (ward != null && !ward.isBlank()) {
      spec = spec.and(PostSpecifications.hasWard(ward));
    }

    if (categoryId != null) {
      spec = spec.and(PostSpecifications.hasCategoryId(categoryId));
    }

    if (brandId != null) {
      spec = spec.and(PostSpecifications.hasBrandId(brandId));
    }

    if (minPrice != null) {
      spec = spec.and(PostSpecifications.minPrice(minPrice));
    }

    if (maxPrice != null) {
      spec = spec.and(PostSpecifications.maxPrice(maxPrice));
    }

    if (tag != null && !tag.isBlank()) {
      spec = spec.and(PostSpecifications.hasTag(tag));
    }
    return postRepository.findAll(spec, pageable).map(this::mapToResponse);
  }

  @Override
  public List<PostResponse> searchPostsNoPage(
      String keyword,
      String title,
      String description,
      String authorName,
      String address,
      String province,
      String ward,
      Long categoryId,
      Long brandId,
      Double minPrice,
      Double maxPrice,
      String tag
  ) {
    Specification<Post> spec = Specification.where(null);

    if (keyword != null && !keyword.isBlank()) {
      Specification<Post> keywordSpec = Specification.where(
          PostSpecifications.hasTitle(keyword)
      ).or(PostSpecifications.hasDescription(keyword))
       .or(PostSpecifications.hasAuthorFullName(keyword))
       .or(PostSpecifications.hasAddressLine(keyword));
      spec = spec.and(keywordSpec);
    }

    if (title != null && !title.isBlank()) {
      spec = spec.and(PostSpecifications.hasTitle(title));
    }

    if (description != null && !description.isBlank()) {
      spec = spec.and(PostSpecifications.hasDescription(description));
    }

    if (authorName != null && !authorName.isBlank()) {
      spec = spec.and(PostSpecifications.hasAuthorFullName(authorName));
    }

    if (address != null && !address.isBlank()) {
      spec = spec.and(PostSpecifications.hasAddressLine(address));
    }

    if (province != null && !province.isBlank()) {
      spec = spec.and(PostSpecifications.hasProvince(province));
    }

    if (ward != null && !ward.isBlank()) {
      spec = spec.and(PostSpecifications.hasWard(ward));
    }

    if (categoryId != null) {
      List<Long> categoryIds = getAllCategoryIds(categoryId);
      spec = spec.and(
          PostSpecifications
              .hasCategoryIds(categoryIds)
      );
    }

    if (brandId != null) {
      spec = spec.and(PostSpecifications.hasBrandId(brandId));
    }

    if (minPrice != null) {
      spec = spec.and(PostSpecifications.minPrice(minPrice));
    }

    if (maxPrice != null) {
      spec = spec.and(PostSpecifications.maxPrice(maxPrice));
    }

    if (tag != null && !tag.isBlank()) {
      spec = spec.and(PostSpecifications.hasTag(tag));
    }

    return postRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional
  public PostResponse approvePost(Long postId, Long adminId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (post.getStatus() == PostStatus.APPROVED) {
      throw new ResException(ResErrorCode.POST_ALREADY_APPROVED);
    }

    post.setStatus(PostStatus.APPROVED);
    post.setApprovedBy(adminId);
    post.setApprovedAt(LocalDateTime.now());
    post.setRejectedReason(null);

    Post saved = postRepository.save(post);

    User admin = userRepository.findById(adminId).orElse(null);
    notificationService.createNotification(
        post.getUser(),
        admin,
        NotificationType.POST_APPROVED,
        "Bài đăng của bạn đã được duyệt",
        "Bài đăng \"" + post.getTitle() + "\" đã được duyệt và hiển thị công khai",
        "/post/" + postId,
        Map.of("postId", postId)
    );

    return mapToResponse(saved);
  }

  @Override
  @Transactional
  public PostResponse rejectPost(Long postId, String reason, Long adminId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (post.getStatus() == PostStatus.REJECTED) {
      throw new ResException(ResErrorCode.POST_ALREADY_REJECTED);
    }

    post.setStatus(PostStatus.REJECTED);
    post.setApprovedBy(adminId);
    post.setApprovedAt(LocalDateTime.now());
    post.setRejectedReason(reason);

    Post saved = postRepository.save(post);

    User admin = userRepository.findById(adminId).orElse(null);
    notificationService.createNotification(
        post.getUser(),
        admin,
        NotificationType.POST_REJECTED,
        "Bài đăng của bạn đã bị từ chối",
        "Bài đăng \"" + post.getTitle() + "\" đã bị từ chối. Lý do: " + (reason != null ? reason : "Không có"),
        "/post/" + postId,
        Map.of("postId", postId, "reason", reason != null ? reason : "")
    );

    return mapToResponse(saved);
  }

  @Override
  public List<PostResponse> getLatestPosts() {
    return postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.APPROVED)
        .stream()
        .limit(20)
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public List<PostResponse> searchPostsByCategory(Long categoryId) {
    return searchPostsByCategory(categoryId, null);
  }

  @Override
  public Page<PostResponse> getPostsPage(Pageable pageable, Long userId) {
    return postRepository.findAll(pageable).map(post -> mapToResponse(post, userId));
  }

  @Override
  public List<PostResponse> getApprovedPosts(String keyword, Long categoryId, Long brandId, String ward, String province, Long userId) {
    Specification<Post> spec = Specification.where(PostSpecifications.hasStatus(PostStatus.APPROVED));

    if (keyword != null && !keyword.isBlank()) {
      spec = spec.and(PostSpecifications.hasKeyword(keyword));
    }
    if (categoryId != null) {
      spec = spec.and(PostSpecifications.hasCategoryId(categoryId));
    }
    if (brandId != null) {
      spec = spec.and(PostSpecifications.hasBrandId(brandId));
    }
    if (ward != null && !ward.isBlank()) {
      spec = spec.and(PostSpecifications.hasWard(ward));
    }
    if (province != null && !province.isBlank()) {
      spec = spec.and(PostSpecifications.hasProvince(province));
    }

    return postRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
        .stream()
        .map(post -> mapToResponse(post, userId))
        .toList();
  }

  @Override
  public List<PostResponse> getLatestPosts(Long userId) {
    return postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.APPROVED)
        .stream()
        .limit(20)
        .map(post -> mapToResponse(post, userId))
        .toList();
  }

  @Override
  public List<PostResponse> searchPostsByCategory(Long categoryId, Long userId) {
    Specification<Post> spec = Specification.where(PostSpecifications.hasStatus(PostStatus.APPROVED));
    if (categoryId != null) {
      spec = spec.and(PostSpecifications.hasCategoryId(categoryId));
    }
    List<Post> posts = postRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    
    // Batch fetch reaction counts
    List<Long> postIds = posts.stream().map(Post::getPostId).toList();
    Map<Long, Long> reactionCounts = postReactionRepository.countReactionsByPostIds(postIds)
        .stream()
        .collect(java.util.stream.Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));
    Map<Long, String> topReactions = postReactionRepository.countReactionsByPostIdsGroupByType(postIds)
        .stream()
        .collect(java.util.stream.Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> ((com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType) arr[1]).name(),
            (existing, replacement) -> existing // keep first if duplicate
        ));
    
    return posts.stream()
        .map(post -> mapToResponse(post, userId, 
            reactionCounts.getOrDefault(post.getPostId(), 0L),
            topReactions.get(post.getPostId())))
        .toList();
  }

  private PostResponse mapToResponse(Post post, Long userId, Long reactionCount, String topReaction) {
    ReactionType currentUserReaction = null;
    if (userId != null) {
      Optional<PostReaction> reaction = postReactionRepository.findByPostPostIdAndUserUserId(post.getPostId(), userId);
      currentUserReaction = reaction.map(PostReaction::getReactionType).orElse(null);
    }

    long commentCount = commentRepository.countByPostId(post.getPostId());

    return PostResponse.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .description(post.getDescription())
        .price(post.getPrice())
        .originalPrice(post.getOriginalPrice())
        .conditionGrade(post.getConditionGrade())
        .status(post.getStatus())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .approvedBy(post.getApprovedBy())
        .approvedAt(post.getApprovedAt())
        .rejectedReason(post.getRejectedReason())
        .currentUserReaction(currentUserReaction != null ? currentUserReaction.name() : null)
        .reactionCount(reactionCount)
        .topReaction(topReaction)
        .category(post.getCategory() == null ? null :
            PostResponse.CategoryInfo.builder()
                .categoryId(post.getCategory().getCategoryId())
                .name(post.getCategory().getName())
                .iconUrl(post.getCategory().getIconUrl())
                .build())
        .brand(post.getBrand() == null ? null :
            PostResponse.BrandInfo.builder()
                .brandId(post.getBrand().getBrandId())
                .name(post.getBrand().getName())
                .logoUrl(post.getBrand().getLogoUrl())
                .build())
        .author(PostResponse.AuthorInfo.builder()
            .userId(post.getUser().getUserId())
            .fullName(post.getUser().getFullName())
            .avatarUrl(post.getUser().getAvatarUrl())
            .phone(post.getUser().getPhone())
            .build())
        .address(post.getAddress() == null ? null :
            PostResponse.AddressInfo.builder()
                .addressId(post.getAddress().getAddressId())
                .fullAddress(post.getAddress().getAddressLine())
                .district(post.getAddress().getWard())
                .city(post.getAddress().getProvince())
                .build())
        .images(post.getImages() == null ? List.of() :
            post.getImages().stream().map(img ->
                PostResponse.PostImageInfo.builder()
                    .imageId(img.getImageId())
                    .mediaType(img.getMediaType())
                    .publicUrl(buildPublicUrl(img.getObjectKey()))
                    .thumbnailUrl(img.getThumbnailKey() != null
                        ? buildPublicUrl(img.getThumbnailKey()) : null)
                    .sortOrder(img.getSortOrder())
                    .width(img.getWidth())
                    .height(img.getHeight())
                    .duration(img.getDuration())
                    .build()
            ).toList())
        .attributes(post.getAttributes() == null ? List.of() :
            post.getAttributes().stream().map(attr ->
                PostResponse.PostAttributeInfo.builder()
                    .attributeKey(attr.getAttributeKey())
                    .attributeValue(attr.getAttributeValue())
                    .sortOrder(attr.getSortOrder())
                    .build()
            ).toList())
        .tags(post.getPostTags() == null ? List.of() :
            post.getPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList())
        .commentCount(commentCount)
        .build();
  }

  private PostResponse mapToResponse(Post post, Long userId) {
    return mapToResponse(post, userId, 0L, null);
  }

  private PostResponse mapToResponse(Post post) {
    return mapToResponse(post, null);
  }

  private String slugify(String input) {
    return input.trim()
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-");
  }
  private List<Long> getAllCategoryIds(Long parentId) {

    List<Long> ids = new ArrayList<>();
    ids.add(parentId);
    List<Category> children = categoryRepository
        .findByParent_CategoryId(parentId);
    ids.addAll(
        children.stream()
            .map(Category::getCategoryId)
            .toList()
    );
    return ids;
  }

  @Override
  public PostDetailUser getPostDetailUser(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    User author = post.getUser();

    Double avgRating = userReviewRepository.getAverageRatingByUserId(author.getUserId());
    long followerCount = userFollowRepository.countFollowersByUserId(author.getUserId());
    long postCount = postRepository.countByUserUserId(author.getUserId());

    float rating = avgRating != null ? avgRating.floatValue() : 0f;
    float responseRate = 0f;
    if (postCount > 0 && followerCount > 0) {
      responseRate = Math.min((float) followerCount / postCount, 5f);
    }

    List<Post> authorPosts = postRepository.findByUserUserId(author.getUserId());
    List<Long> authorPostIds = authorPosts.stream()
        .filter(p -> p.getPostId() != null)
        .map(Post::getPostId)
        .toList();

    Map<Long, Long> reactionCounts = postReactionRepository.countReactionsByPostIds(authorPostIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    Map<Long, Long> commentCounts = commentRepository.countCommentsByPostIds(authorPostIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));

    List<PostDetailUser.PostSeller> postSellerList = authorPosts.stream()
        .filter(p -> p.getPostId() != null)
        .map(p -> {
          String thumbnailUrl = null;
          String mediaType = null;
          if (p.getImages() != null && !p.getImages().isEmpty()) {
            PostImage firstImage = p.getImages().get(0);
            thumbnailUrl = buildPublicUrl(firstImage.getObjectKey());
            mediaType = firstImage.getMediaType() != null ? firstImage.getMediaType().name() : "IMAGE";
          }
          String formattedPrice = p.getPrice() != null
              ? currencyFormat.format(p.getPrice()).replace("₫", "").trim() + "₫"
              : null;
          String postedAt = formatRelativeTimeInternal(p.getCreatedAt());
          return PostDetailUser.PostSeller.builder()
              .postId(p.getPostId())
              .title(p.getTitle())
              .price(p.getPrice() != null ? p.getPrice().toString() : null)
              .formattedPrice(formattedPrice)
              .thumbnailUrl(thumbnailUrl)
              .mediaType(mediaType)
              .countReaction(reactionCounts.getOrDefault(p.getPostId(), 0L))
              .countComment(commentCounts.getOrDefault(p.getPostId(), 0L))
              .postedAt(postedAt)
              .build();
        })
        .toList();

    String avatarUrl = author.getAvatarUrl();
    if (avatarUrl != null && !avatarUrl.startsWith("http")) {
      avatarUrl = buildPublicUrl(avatarUrl);
    }
    String addressLine = post.getAddress() != null ? (post.getAddress().getWard() + ", "+ post.getAddress().getProvince() ): null;
    return PostDetailUser.builder()
        .userId(author.getUserId())
        .username(author.getFullName())
        .email(author.getEmail())
        .createDate(author.getCreatedAt() != null ? author.getCreatedAt().toString() : null)
        .avatarUrl(avatarUrl)
        .addressLine(addressLine)
        .bio(author.getBio())
        .rating(rating)
        .responseRate(responseRate)
        .trustScore(author.getTrustScore() != null ? author.getTrustScore() : 0)
        .countFlow((float) followerCount)
        .postSellerList(postSellerList)
        .build();
  }

  @Override
  public List<PostResponse> hotPost() {
    List<Post> approvedPosts = postRepository.findByStatusOrderByCreatedAtAsc(PostStatus.APPROVED);

    if (approvedPosts.isEmpty()) {
      return List.of();
    }

    List<Long> postIds = approvedPosts.stream()
        .map(Post::getPostId)
        .toList();

    Map<Long, Long> reactionCountMap = postReactionRepository.countReactionsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    Map<Long, Long> commentCountMap = commentRepository.countCommentsByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(
            arr -> (Long) arr[0],
            arr -> (Long) arr[1]
        ));

    List<Post> hotPosts = approvedPosts.stream()
        .sorted((p1, p2) -> {
          long r1 = reactionCountMap.getOrDefault(p1.getPostId(), 0L);
          long c1 = commentCountMap.getOrDefault(p1.getPostId(), 0L);
          long r2 = reactionCountMap.getOrDefault(p2.getPostId(), 0L);
          long c2 = commentCountMap.getOrDefault(p2.getPostId(), 0L);
          return Long.compare(r2 + c2, r1 + c1);
        })
        .limit(20)
        .toList();
    return hotPosts.stream()
        .map(post -> mapToResponse(post))
        .toList();
  }

  @Override
  public List<PostResponse> getMyPostsByStatus(Long userId, PostStatus status) {
    List<Post> posts;
    if (status != null) {
      posts = postRepository.findByUserUserIdAndStatus(userId, status);
    } else {
      posts = postRepository.findByUserUserId(userId);
    }
    return posts.stream()
        .map(post -> mapToResponse(post, userId))
        .toList();
  }

  @Override
  @Transactional
  public PostResponse hidePost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    post.setStatus(PostStatus.HIDDEN);
    Post saved = postRepository.save(post);
    return mapToResponse(saved, userId);
  }

  @Override
  @Transactional
  public PostResponse markAsSold(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    post.setStatus(PostStatus.SOLD);
    Post saved = postRepository.save(post);
    return mapToResponse(saved, userId);
  }

  @Override
  @Transactional
  public PostResponse unhidePost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getUserId().equals(userId)) {
      throw new ResException(ResErrorCode.PERMISSION_DENIED);
    }

    post.setStatus(PostStatus.APPROVED);
    Post saved = postRepository.save(post);
    return mapToResponse(saved, userId);
  }

  @Override
  public Page<PostResponse> getPendingPosts(String keyword, Pageable pageable) {
    Specification<Post> spec = Specification.where(PostSpecifications.hasStatus(PostStatus.PENDING));

    if (keyword != null && !keyword.isBlank()) {
      Specification<Post> keywordSpec = Specification.where(
          PostSpecifications.hasTitle(keyword)
      ).or(PostSpecifications.hasDescription(keyword))
       .or(PostSpecifications.hasAuthorFullName(keyword));
      spec = spec.and(keywordSpec);
    }

    return postRepository.findAll(spec, pageable).map(this::mapToResponse);
  }

  private String formatRelativeTimeInternal(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "Không rõ";
    }
    LocalDateTime now = LocalDateTime.now();
    long minutes = java.time.Duration.between(dateTime, now).toMinutes();
    long hours = java.time.Duration.between(dateTime, now).toHours();
    long days = java.time.Duration.between(dateTime, now).toDays();

    if (minutes < 1) return "Vừa xong";
    if (minutes < 60) return minutes + " phút trước";
    if (hours < 24) return hours + " giờ trước";
    if (days < 7) return days + " ngày trước";
    if (days < 30) return (days / 7) + " tuần trước";
    if (days < 365) return (days / 30) + " tháng trước";
    return (days / 365) + " năm trước";
  }
}

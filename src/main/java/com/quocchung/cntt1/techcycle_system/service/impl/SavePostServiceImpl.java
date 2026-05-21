package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.SavePost.SavePostResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostCollection;
import com.quocchung.cntt1.techcycle_system.model.PostReaction;
import com.quocchung.cntt1.techcycle_system.model.SavedPost;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.PostCollectionRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostReactionRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.SavePostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.SavePostService;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class SavePostServiceImpl implements SavePostService {
    private final SavePostRepository savePostRepository;
    private final PostCollectionRepository postCollectionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostReactionRepository postReactionRepository;
    private final MinioProperties minioProperties;


    @Override
    public SavePostResponse savePort(Long userId, Long postId, Long collectionId) {
        PostCollection collection = postCollectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));
        if (!collection.getUser().getUserId().equals(userId)) {
            throw new ResException(ResErrorCode.PERMISSION_DENIED);
        }
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND));

        boolean alreadySaved = savePostRepository
            .existsByUserUserIdAndPostPostIdAndCollectionCollectionId(userId, postId, collectionId);
        if (alreadySaved) {
            throw new ResException(ResErrorCode.ENTITY_EXISTED);
        }

        if (post.getUser().getUserId().equals(userId)) {
            throw new ResException(ResErrorCode.CANNOT_SAVE_OWN_POST);
        }

        User user = userRepository.getReferenceById(userId);


        SavedPost savedPost = SavedPost.builder()
            .user(user)
            .post(post)
            .collection(collection)
            .notifyOnUpdate(false)
            .build();

        SavedPost saved = savePostRepository.save(savedPost);
        return mapToResponse(saved);


    }
    @Override
    public void unSavePort(Long userId, Long postId, Long collectionId) {
        SavedPost savedPost = savePostRepository
            .findByUserUserIdAndPostPostIdAndCollectionCollectionId(userId, postId, collectionId)
            .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));

        savePostRepository.delete(savedPost);
    }

    @Override
    public List<PostResponse> getAllSavePostByCollection(Long userId, Long collectionId) {
        // Kiểm tra collection thuộc về user
        PostCollection collection = postCollectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));

        if (!collection.getUser().getUserId().equals(userId)) {
            throw new ResException(ResErrorCode.PERMISSION_DENIED);
        }

        List<SavedPost> savedPosts = savePostRepository
            .findAllByUserUserIdAndCollectionCollectionId(userId, collectionId);

        return savedPosts.stream()
            .map(sp -> mapToResponse(sp.getPost(), userId))
            .toList();
    }

    private SavePostResponse mapToResponse(SavedPost savedPost) {
        return SavePostResponse.builder()
            .savedId(savedPost.getSavedId())
            .postId(savedPost.getPost().getPostId())
            .collectionId(savedPost.getCollection().getCollectionId())
            .notifyOnUpdate(savedPost.getNotifyOnUpdate())
            .savedAt(savedPost.getSavedAt())
            .build();
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


    private PostResponse mapToResponse(Post post, Long userId) {
        ReactionType currentUserReaction = null;
        if (userId != null) {
            Optional<PostReaction> reaction = postReactionRepository.findByPostPostIdAndUserUserId(post.getPostId(), userId);
            currentUserReaction = reaction.map(PostReaction::getReactionType).orElse(null);
        }

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
            .build();
    }
}

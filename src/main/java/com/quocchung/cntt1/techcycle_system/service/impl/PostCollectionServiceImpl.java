package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.PostCollection.PostCollectionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.PostCollectionResponse.PostCollectionResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.PostCollection;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.PostCollectionRepository;
import com.quocchung.cntt1.techcycle_system.repository.SavePostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.PostCollectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCollectionServiceImpl implements PostCollectionService {

    private final PostCollectionRepository postCollectionRepository;
    private final UserRepository userRepository;
    private final SavePostRepository savePostRepository;

    @Override
    public PostCollectionResponse addPostCollection(Long userId, PostCollectionRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND));

        boolean nameExists = postCollectionRepository.existsByUserUserIdAndName(userId, request.getName());
        if (nameExists) {
            throw new ResException(ResErrorCode.ENTITY_EXISTED);
        }

        PostCollection collection = PostCollection.builder()
            .user(user)
            .name(request.getName())
            .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
            .build();

        PostCollection saved = postCollectionRepository.save(collection);
        return mapToResponse(saved);
    }

    @Override
    public PostCollectionResponse updatePostCollection(Long id, Long userId, PostCollectionRequest request) {
        PostCollection collection = postCollectionRepository.findById(id)
            .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));

        if (!collection.getUser().getUserId().equals(userId)) {
            throw new ResException(ResErrorCode.PERMISSION_DENIED);
        }

        boolean nameExists = postCollectionRepository.existsByUserUserIdAndNameAndCollectionIdNot(
            userId, request.getName(), id
        );
        if (nameExists) {
            throw new ResException(ResErrorCode.ENTITY_EXISTED);
        }

        collection.setName(request.getName());
        if (request.getIsDefault() != null) {
            collection.setIsDefault(request.getIsDefault());
        }

        PostCollection updated = postCollectionRepository.save(collection);
        return mapToResponse(updated);
    }

    @Override
    public void deletePostCollection(Long userId, Long id) {
        PostCollection collection = postCollectionRepository.findById(id)
            .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS));

        if (!collection.getUser().getUserId().equals(userId)) {
            throw new ResException(ResErrorCode.PERMISSION_DENIED);
        }

        if (Boolean.TRUE.equals(collection.getIsDefault())) {
            throw new ResException(ResErrorCode.PERMISSION_DENIED);
        }
        savePostRepository.deleteAllByCollectionCollectionId(id);
        postCollectionRepository.delete(collection);
    }

    @Override
    public List<PostCollectionResponse> getAll(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResException(ResErrorCode.USER_NOT_FOUND);
        }

        List<PostCollection> collections = postCollectionRepository.findAllByUserUserId(userId);
        return collections.stream()
            .map(this::mapToResponse)
            .toList();
    }

    private PostCollectionResponse mapToResponse(PostCollection collection) {
        return PostCollectionResponse.builder()
            .collectionId(collection.getCollectionId())
            .name(collection.getName())
            .isDefault(collection.getIsDefault())
            .createdAt(collection.getCreatedAt())
            .build();
    }
}

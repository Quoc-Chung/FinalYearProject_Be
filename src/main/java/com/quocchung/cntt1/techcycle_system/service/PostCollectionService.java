package com.quocchung.cntt1.techcycle_system.service;


import com.quocchung.cntt1.techcycle_system.dtos.request.PostCollection.PostCollectionRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.PostCollectionResponse.PostCollectionResponse;
import java.util.List;

public interface PostCollectionService {

  PostCollectionResponse addPostCollection(Long userId, PostCollectionRequest addPostCollectionRequest);

  PostCollectionResponse updatePostCollection(Long id, Long userId, PostCollectionRequest addPostCollectionRequest);

  void deletePostCollection(Long userId, Long id);

  List<PostCollectionResponse> getAll(Long userId);
}

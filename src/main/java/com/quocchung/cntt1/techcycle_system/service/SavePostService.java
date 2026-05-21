package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.SavePost.SavePostResponse;
import java.util.List;

public interface SavePostService {

  SavePostResponse savePort(Long userId, Long postId, Long collectionId);


  void unSavePort(Long userId, Long postId, Long collectionId);

  List<PostResponse> getAllSavePostByCollection(Long userId, Long collectionId);
}

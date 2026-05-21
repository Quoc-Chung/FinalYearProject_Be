package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import com.quocchung.cntt1.techcycle_system.dtos.response.Post.PostResponse.PostImageInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//dữ liệu các bài viết mà bạn theo dõi
public class PostUserFollowResponse {

  private Long postId;
  private Long userId;
  private String fullName; // nếu null lấy username
  private String avatarUrl;
  private String address; // ward, province
  private String title;
  private Long countReaction;
  private Long countComment;
  private Long price;
  private List<PostImageInfo> images;

}

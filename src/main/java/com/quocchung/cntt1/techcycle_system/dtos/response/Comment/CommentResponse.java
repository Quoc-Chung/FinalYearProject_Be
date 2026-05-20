package com.quocchung.cntt1.techcycle_system.dtos.response.Comment;

import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
  private Long commentId;
  private String userNameComment;  // fullname
  private String userAvatarUrl;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long countReactionComent;
  private String userReactionComment; // user hiện tại thả cảm xúc gì
  private Boolean isPinned;
  private Boolean isHidden;
  private List<MediaInfo> mediaInfoList;
  private List<CommentResponse> commentChildrenResponse;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MediaInfo {
    private Long imageId;
    private MediaType mediaType;
    private String publicUrl;
    private String thumbnailUrl;  // null nếu IMAGE
    private Integer sortOrder;
    private Integer width;
    private Integer height;
    private Double duration;      // null nếu IMAGE
  }
}

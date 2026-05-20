package com.quocchung.cntt1.techcycle_system.dtos.request.Comment;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequest {
  private String content;
  private Long userId;
  private Long postId;
  private Long commentParentId;

  private List<MediaEntry> mediaList;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class MediaEntry {
    @NotBlank
    private String objectKey;
    private String thumbnailKey;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private Double duration;
    private Integer sortOrder;
  }
}

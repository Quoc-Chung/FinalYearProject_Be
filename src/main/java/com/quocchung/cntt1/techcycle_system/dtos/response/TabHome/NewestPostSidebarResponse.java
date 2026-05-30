package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewestPostSidebarResponse {
  private Long postId;
  private String title;
  private Long price;
  private String formattedPrice;
  private String thumbnailUrl;
  private String mediaType;
  private Long userId;
  private String userFullName;
  private String userAvatarUrl;
  private Long countReaction;
  private Long countComment;
  private String createdAt;
  private String relativeTime;
}

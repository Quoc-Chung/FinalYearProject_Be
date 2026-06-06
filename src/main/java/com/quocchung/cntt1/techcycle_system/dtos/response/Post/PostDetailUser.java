package com.quocchung.cntt1.techcycle_system.dtos.response.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@AllArgsConstructor
@Builder
public class PostDetailUser {
  private Long userId;
  private String username;
  private String email;
  private String createDate;
  private String avatarUrl;
  private String addressLine;
  private String bio;
  private float rating;
  private float responseRate;
  private Double trustScore;
  private float countFlow;
  private List<PostSeller> postSellerList;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostSeller {
    private Long postId;
    private String title;
    private String price;
    private String formattedPrice;
    private String thumbnailUrl;
    private String mediaType;
    private Long countReaction;
    private Long countComment;
    private String postedAt;
  }
}

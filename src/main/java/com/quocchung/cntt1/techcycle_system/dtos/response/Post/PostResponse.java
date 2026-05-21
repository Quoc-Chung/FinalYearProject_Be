package com.quocchung.cntt1.techcycle_system.dtos.response.Post;
import com.quocchung.cntt1.techcycle_system.utils.enums.ConditionGrade;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

  private Long postId;
  private String title;
  private String description;
  private BigDecimal price;
  private BigDecimal originalPrice;
  private ConditionGrade conditionGrade;
  private PostStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Trả về nếu bị từ chối thì nó sẽ đi kèm lý do
  private String reason;

  private Long approvedBy;
  private LocalDateTime approvedAt;
  private String rejectedReason;

  private String currentUserReaction;

  private CategoryInfo category;
  private BrandInfo brand;
  private AuthorInfo author;
  private AddressInfo address;

  private List<PostImageInfo> images;
  private List<PostAttributeInfo> attributes;
  private List<String> tags;

  private Long commentCount;


  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class CategoryInfo {
    private Long categoryId;
    private String name;
    private String iconUrl;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class BrandInfo {
    private Long brandId;
    private String name;
    private String logoUrl;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class AuthorInfo {
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private String phone;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class AddressInfo {
    private Long addressId;
    private String fullAddress;
    private String district;
    private String city;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor
  public static class PostImageInfo {
    private Long imageId;
    private MediaType mediaType;
    private String publicUrl;
    private String thumbnailUrl;  // null nếu IMAGE
    private Integer sortOrder;
    private Integer width;
    private Integer height;
    private Double duration;      // null nếu IMAGE
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostAttributeInfo {
    private String attributeKey;
    private String attributeValue;
    private Integer sortOrder;
  }
}
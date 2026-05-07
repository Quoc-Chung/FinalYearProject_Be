package com.quocchung.cntt1.techcycle_system.dtos.request.Post;


import com.quocchung.cntt1.techcycle_system.utils.enums.ConditionGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreatePostRequest {
  private String title;

  private String description;

  private BigDecimal price;

  private BigDecimal originalPrice;

  @NotNull(message = "Danh mục không được để trống")
  private Long categoryId;

  private Long brandId;

  @NotNull(message = "Tình trạng không được để trống")
  private ConditionGrade conditionGrade;

  private Long addressId;

  private List<String> tagNames;

  // Thông tin bổ sung (PostAttribute)
  private List<AttributeEntry> attributes;

  private List<MediaEntry> mediaList;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AttributeEntry {
    @NotBlank
    private String attributeKey;
    private String attributeValue;
    private Integer sortOrder;
  }


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

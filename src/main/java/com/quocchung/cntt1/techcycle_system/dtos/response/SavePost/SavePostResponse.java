package com.quocchung.cntt1.techcycle_system.dtos.response.SavePost;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavePostResponse {
  private Long savedId;
  private Long postId;
  private Long collectionId;
  private Boolean notifyOnUpdate;
  private LocalDateTime savedAt;
}
package com.quocchung.cntt1.techcycle_system.dtos.response.PostCollectionResponse;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCollectionResponse {
  private Long collectionId;
  private String name;
  private Boolean isDefault;
  private LocalDateTime createdAt;
}

package com.quocchung.cntt1.techcycle_system.dtos.response.Category;

import com.google.errorprone.annotations.NoAllocation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryTreeResponse {
    private Long categoryId;
    private Long parentId;
    private String parentName;
    private String name;
    private String iconUrl;
    private Boolean isActive;

    private List<CategoryTreeResponse> childrens;
}

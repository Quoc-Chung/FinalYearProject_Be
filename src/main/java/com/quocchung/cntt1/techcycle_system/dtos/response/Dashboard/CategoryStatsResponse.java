package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryStatsResponse {
    private Long categoryId;
    private String categoryName;
    private Long postCount;           // Tổng số bài trong danh mục
    private Long approvedCount;       // Số bài đã duyệt
    private Long soldCount;           // Số bài đã bán
    private Long pendingCount;        // Số bài chờ duyệt
    private Double percentage;        // Tỷ lệ % so với tổng
}

package com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentUserResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String status;
    private String createdAt;
    private Long postCount;
    private Double trustScore;
}

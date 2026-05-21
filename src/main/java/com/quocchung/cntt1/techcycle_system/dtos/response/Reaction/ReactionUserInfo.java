package com.quocchung.cntt1.techcycle_system.dtos.response.Reaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionUserInfo {

    private Long userId;
    private String fullName;
    private String ward;
    private String province;
    private String avatarUrl;
    private Double trustScore;
}

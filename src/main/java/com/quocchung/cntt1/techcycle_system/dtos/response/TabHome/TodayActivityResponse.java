package com.quocchung.cntt1.techcycle_system.dtos.response.TabHome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayActivityResponse {
    private Long countPort;
    private Long newUser;
    private Long countReaction;
    private Long numberOfVisits;
    private Long countApproved;
    private Long countRejected;
}

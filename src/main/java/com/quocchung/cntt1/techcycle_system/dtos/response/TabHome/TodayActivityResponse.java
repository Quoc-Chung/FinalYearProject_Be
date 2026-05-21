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
    private Long  newUser;
    private Long  countReaction;  // số luopwjt tương tasc của tất cả bài viết trong hoome nay
    private Long  numberOfVisits; // số lượt truy cập


}

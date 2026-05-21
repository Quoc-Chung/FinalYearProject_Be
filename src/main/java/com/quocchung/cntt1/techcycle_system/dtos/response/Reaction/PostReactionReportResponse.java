package com.quocchung.cntt1.techcycle_system.dtos.response.Reaction;

import com.quocchung.cntt1.techcycle_system.utils.enums.ReactionType;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReactionReportResponse {

    private Long postId;
    private Long totalReactions;
    private Map<ReactionType, Long> reactionCounts;
    private ReactionType topReaction;
    private Long likeCount;
    private Long loveCount;
    private Long hahaCount;
    private Long wowCount;
    private Long sadCount;
    private Long angryCount;

    private List<ReactionUserInfo> likeUsers;
    private List<ReactionUserInfo> loveUsers;
    private List<ReactionUserInfo> hahaUsers;
    private List<ReactionUserInfo> wowUsers;
    private List<ReactionUserInfo> sadUsers;
    private List<ReactionUserInfo> angryUsers;
}

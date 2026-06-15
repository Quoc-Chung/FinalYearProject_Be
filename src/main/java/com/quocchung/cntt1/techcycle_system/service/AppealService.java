package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateAppealRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.AppealResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import org.springframework.data.domain.Page;

public interface AppealService {

    AppealResponse createAppeal(Long userId, CreateAppealRequest request);

    AppealResponse getAppealById(Long appealId);

    Page<AppealResponse> getAppealsByStatus(AppealStatus status, int page, int size);

    Page<AppealResponse> getAllAppeals(int page, int size);

    Page<AppealResponse> getAppealsByUser(Long userId, int page, int size);

    boolean hasUserAppealedPost(Long userId, Long postId);

    AppealResponse processAppeal(Long appealId, Long adminId, AppealStatus decision, String adminNote);

    long countPendingAppeals();
}

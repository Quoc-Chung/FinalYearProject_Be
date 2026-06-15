package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateAppealRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.AppealResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.AppealService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.enums.AppealStatus;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<APIResponse<AppealResponse>> createAppeal(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateAppealRequest request) {
        Long userId =  userPrincipal.getUserId();
        AppealResponse response = appealService.createAppeal(userId, request);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{appealId}")
    public ResponseEntity<APIResponse<AppealResponse>> getAppealById(@PathVariable Long appealId) {
        AppealResponse response = appealService.getAppealById(appealId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<AppealResponse>> getAppeals(
            @RequestParam(required = false) AppealStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppealResponse> result;
        if (status != null) {
            result = appealService.getAppealsByStatus(status, page, size);
        } else {
            result = appealService.getAllAppeals(page, size);
        }
        return ResponseEntity.ok(
            responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/my-appeals")
    public ResponseEntity<APIResponse<AppealResponse>> getMyAppeals(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId =  userPrincipal.getUserId();
        Page<AppealResponse> result = appealService.getAppealsByUser(userId, page, size);
        return ResponseEntity.ok(
            responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<APIResponse<Boolean>> checkUserAppealed(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId) {
        Long userId =  userPrincipal.getUserId();
        boolean hasAppealed = appealService.hasUserAppealedPost(userId, postId);
        return ResponseEntity.ok(responseUtils.success(hasAppealed));
    }

    @PutMapping("/{appealId}/process")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<AppealResponse>> processAppeal(
            @PathVariable Long appealId,
            @RequestParam AppealStatus decision,
            @RequestParam(required = false) String adminNote,
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long adminId =  userPrincipal.getUserId();
        AppealResponse response = appealService.processAppeal(appealId, adminId, decision, adminNote);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/count/pending")
    public ResponseEntity<APIResponse<Long>> countPendingAppeals() {
        long count = appealService.countPendingAppeals();
        return ResponseEntity.ok(responseUtils.success(count));
    }
}

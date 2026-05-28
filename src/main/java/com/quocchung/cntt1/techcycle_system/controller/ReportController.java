package com.quocchung.cntt1.techcycle_system.controller;


import com.quocchung.cntt1.techcycle_system.dto.response.FullReportResponse;

import com.quocchung.cntt1.techcycle_system.dto.response.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dto.response.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ReportService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final ResponseUtils responseUtils;

    @PostMapping("/post/{postId}")
    public ResponseEntity<APIResponse<ReportResponse>> createReport(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse result = reportService.createReport(postId, userPrincipal.getUserId(), request);
        return ResponseEntity.ok(responseUtils.success(result));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<APIResponse<ReportResponse>> getReportsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReportResponse> result = reportService.getReportsByPost(postId, page, size);
        return ResponseEntity.ok(
                responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<APIResponse<ReportResponse>> getMyReports(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReportResponse> result = reportService.getReportsByReporter(userPrincipal.getUserId(), page, size);
        return ResponseEntity.ok(
                responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<ReportResponse>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReportResponse> result = reportService.getReportsByStatus(status, page, size);
        return ResponseEntity.ok(
                responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
    }

    @PutMapping("/{reportId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<ReportResponse>> resolveReport(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReportResponse result = reportService.resolveReport(reportId, userPrincipal.getUserId(), status);
        return ResponseEntity.ok(responseUtils.success(result));
    }

    // ========== ADMIN STATS ENDPOINTS ==========

    @GetMapping("/full")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<FullReportResponse>> getFullReport() {
        FullReportResponse report = reportService.getFullReport();
        return ResponseEntity.ok(responseUtils.success(report));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<List<UserReportStatsResponse>>> getUserReportStats(
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserReportStatsResponse> users = reportService.getUserReportStats(searchText, page, size);
        return ResponseEntity.ok(responseUtils.success(users));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<UserReportStatsResponse>> getUserStats(@PathVariable Long userId) {
        UserReportStatsResponse userStats = reportService.getUserStats(userId);
        return ResponseEntity.ok(responseUtils.success(userStats));
    }

    @GetMapping("/users/{userId}/posts")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<APIResponse<List<UserPostDetailResponse>>> getUserPosts(@PathVariable Long userId) {
        List<UserPostDetailResponse> posts = reportService.getUserPosts(userId);
        return ResponseEntity.ok(responseUtils.success(posts));
    }
}

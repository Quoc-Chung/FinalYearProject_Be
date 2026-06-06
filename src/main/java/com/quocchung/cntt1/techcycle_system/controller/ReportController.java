package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.FullReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.ReportService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

  @PutMapping("/posts/{postId}/resolve")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<Void>> resolvePostReports(
      @PathVariable Long postId,
      @RequestParam boolean isViolation,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    reportService.resolvePostReports(postId, userPrincipal.getUserId(), isViolation);
    return ResponseEntity.ok(responseUtils.success(null));
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

  @GetMapping("/admin")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<ReportResponse>> getAdminReports(
      @RequestParam(required = false) ReportStatus status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<ReportResponse> result = reportService.getAdminReports(status, page, size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
  }

  @GetMapping("/full")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<FullReportResponse>> getFullReport() {
    FullReportResponse report = reportService.getFullReport();
    return ResponseEntity.ok(responseUtils.success(report));
  }

  @GetMapping("/users")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<APIResponse<UserReportStatsResponse>> getUserReportStats(
      @RequestParam(required = false) String searchText,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<UserReportStatsResponse> result = reportService.getUserReportStats(searchText, page, size);
    return ResponseEntity.ok(
        responseUtils.successPage(result.getContent(), page, result.getTotalElements(), size));
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

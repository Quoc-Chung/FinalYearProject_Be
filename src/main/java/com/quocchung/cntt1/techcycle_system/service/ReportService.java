package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Dashboard.CategoryStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.FullReportResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Report.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ReportService {

  ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request);

  Page<ReportResponse> getReportsByPost(Long postId, int page, int size);

  Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size);

  Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size);

  Page<ReportResponse> getAdminReports(ReportStatus status, int page, int size);

  void resolvePostReports(Long postId, Long adminId, boolean isViolation);

  boolean checkUserReportedPost(Long postId, Long userId);

  FullReportResponse getFullReport();

  Page<UserReportStatsResponse> getUserReportStats(String searchText, int page, int size);

  List<UserPostDetailResponse> getUserPosts(Long userId);

  UserReportStatsResponse getUserStats(Long userId);

  List<CategoryStatsResponse> getCategoryStats();
}

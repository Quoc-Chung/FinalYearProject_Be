package com.quocchung.cntt1.techcycle_system.service;


import com.quocchung.cntt1.techcycle_system.dto.response.FullReportResponse;

import com.quocchung.cntt1.techcycle_system.dto.response.UserPostDetailResponse;
import com.quocchung.cntt1.techcycle_system.dto.response.UserReportStatsResponse;
import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReportService {

    ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request);

    Page<ReportResponse> getReportsByPost(Long postId, int page, int size);

    Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size);

    Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size);

    ReportResponse resolveReport(Long reportId, Long adminId, ReportStatus status);

    FullReportResponse getFullReport();

    List<UserReportStatsResponse> getUserReportStats(String searchText, int page, int size);

    List<UserPostDetailResponse> getUserPosts(Long userId);

    UserReportStatsResponse getUserStats(Long userId);
}

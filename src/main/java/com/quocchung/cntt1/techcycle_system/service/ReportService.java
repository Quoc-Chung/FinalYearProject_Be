package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import org.springframework.data.domain.Page;

public interface ReportService {

    ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request);

    Page<ReportResponse> getReportsByPost(Long postId, int page, int size);

    Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size);

    Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size);

    ReportResponse resolveReport(Long reportId, Long adminId, ReportStatus status);
}

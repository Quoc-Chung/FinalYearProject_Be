package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.CreateReportRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.ReportResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.model.PostReport;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.PostReportRepository;
import com.quocchung.cntt1.techcycle_system.repository.PostRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.ReportService;
import com.quocchung.cntt1.techcycle_system.utils.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportResponse createReport(Long postId, Long reporterId, CreateReportRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));

        if (post.getUser().getUserId().equals(reporterId)) {
            throw new ResException(ResErrorCode.INVALID_REQUEST,
                    "Bạn không thể tự báo cáo bài đăng của chính mình");
        }

        if (postReportRepository.existsByPostPostIdAndReporterUserId(postId, reporterId)) {
            throw new ResException(ResErrorCode.INVALID_REQUEST,
                    "Bạn đã báo cáo bài đăng này rồi");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng với id: " + reporterId));

        PostReport report = PostReport.builder()
                .post(post)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        report = postReportRepository.save(report);

        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByPost(Long postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy bài đăng với id: " + postId));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByPostPostIdOrderByCreatedAtDesc(postId, pageable);
        return reports.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByReporter(Long reporterId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByReporterUserId(reporterId, pageable);
        return reports.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostReport> reports = postReportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return reports.map(this::toResponse);
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(Long reportId, Long adminId, ReportStatus status) {
        PostReport report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new ResException(ResErrorCode.POST_NOT_FOUND,
                        "Không tìm thấy báo cáo với id: " + reportId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResException(ResErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy admin với id: " + adminId));

        report.setStatus(status);
        report.setResolvedBy(admin);
        report = postReportRepository.save(report);

        return toResponse(report);
    }

    private ReportResponse toResponse(PostReport report) {
        ReportResponse.ReporterInfo reporterInfo = ReportResponse.ReporterInfo.builder()
                .userId(report.getReporter().getUserId())
                .fullName(report.getReporter().getFullName())
                .avatarUrl(report.getReporter().getAvatarUrl())
                .build();

        return ReportResponse.builder()
                .reportId(report.getReportId())
                .postId(report.getPost().getPostId())
                .postTitle(report.getPost().getTitle())
                .reporter(reporterInfo)
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

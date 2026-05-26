package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.NotificationSettingRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationSettingResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final ResponseUtils responseUtils;

  @GetMapping
  public ResponseEntity<APIResponse<NotificationResponse>> getNotifications(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<NotificationResponse> notifications = notificationService.getNotifications(
        userPrincipal.getUserId(), pageable
    );
    List<NotificationResponse> content = notifications.getContent();
    return ResponseEntity.ok(
        responseUtils.successPage(content, page, notifications.getTotalElements(), size)
    );
  }

  @GetMapping("/recent")
  public ResponseEntity<APIResponse<NotificationResponse>> getRecentNotifications(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "10") int limit
  ) {
    Page<NotificationResponse> notifications = notificationService.getRecentNotifications(
        userPrincipal.getUserId(), limit
    );
    return ResponseEntity.ok(responseUtils.successList(notifications.getContent()));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<APIResponse<Long>> getUnreadCount(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    long count = notificationService.getUnreadCount(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(count));
  }

  @GetMapping("/filter")
  public ResponseEntity<APIResponse<NotificationResponse>> getNotificationsByReadStatus(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam Boolean isRead,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<NotificationResponse> notifications = notificationService.getNotificationsByReadStatus(
        userPrincipal.getUserId(), isRead, pageable
    );
    List<NotificationResponse> content = notifications.getContent();
    return ResponseEntity.ok(
        responseUtils.successPage(content, page, notifications.getTotalElements(), size)
    );
  }

  @PutMapping("/read-all")
  public ResponseEntity<APIResponse<Integer>> markAllAsRead(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    int count = notificationService.markAllAsRead(userPrincipal.getUserId());
    return ResponseEntity.ok(responseUtils.success(count));
  }

  @PutMapping("/{notificationId}/read")
  public ResponseEntity<APIResponse<NotificationResponse>> markAsRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    return notificationService.markAsRead(notificationId, userPrincipal.getUserId())
        .map(notification -> ResponseEntity.ok(responseUtils.success(notification)))
        .orElse(ResponseEntity.notFound().build());
  }

  // ==================== Notification Settings ====================

  @GetMapping("/settings")
  public ResponseEntity<APIResponse<NotificationSettingResponse>> getNotificationSettings(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    List<NotificationSettingResponse> settings = notificationService.getNotificationSettings(
        userPrincipal.getUserId()
    );
    return ResponseEntity.ok(responseUtils.successList(settings));
  }

  @PutMapping("/settings")
  public ResponseEntity<APIResponse<NotificationSettingResponse>> updateNotificationSetting(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody NotificationSettingRequest request
  ) {
    NotificationSettingResponse setting = notificationService.updateNotificationSetting(
        userPrincipal.getUserId(),
        request.getType(),
        request.getEnabled()
    );
    return ResponseEntity.ok(responseUtils.success(setting));
  }

  @PutMapping("/settings/batch")
  public ResponseEntity<APIResponse<NotificationSettingResponse>> updateBatchNotificationSettings(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody List<NotificationSettingRequest> settings
  ) {
    Map<NotificationType, Boolean> settingsMap = settings.stream()
        .collect(Collectors.toMap(
            NotificationSettingRequest::getType,
            NotificationSettingRequest::getEnabled
        ));

    List<NotificationSettingResponse> results = notificationService.updateBatchNotificationSettings(
        userPrincipal.getUserId(),
        settingsMap
    );
    return ResponseEntity.ok(responseUtils.successList(results));
  }
}

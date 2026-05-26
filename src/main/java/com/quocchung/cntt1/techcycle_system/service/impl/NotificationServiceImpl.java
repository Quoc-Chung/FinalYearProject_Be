package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationSettingResponse;
import com.quocchung.cntt1.techcycle_system.model.Notification;
import com.quocchung.cntt1.techcycle_system.model.NotificationPreference;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.NotificationPreferenceRepository;
import com.quocchung.cntt1.techcycle_system.repository.NotificationRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.NotificationService;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationPreferenceRepository notificationPreferenceRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  @Transactional
  public NotificationResponse createNotification(
      User recipient,
      User actor,
      NotificationType type,
      String title,
      String content,
      String targetUrl,
      Map<String, Object> data
  ) {
    Notification notification = Notification.builder()
        .user(recipient)
        .actor(actor)
        .type(type)
        .title(title)
        .content(content)
        .targetUrl(targetUrl)
        .data(data)
        .isRead(false)
        .build();

    Notification saved = notificationRepository.save(notification);
    log.info("[NOTI] Created notification for user {}: type={}, title={}",
        recipient.getUserId(), type, title);

    NotificationResponse response = toResponse(saved);
    sendRealTimeNotification(recipient, response);

    return response;
  }

  @Override
  public void sendRealTimeNotification(User recipient, NotificationResponse notification) {
    try {

      String destination = "/topic/user." + recipient.getUserId() + ".notifications";
      messagingTemplate.convertAndSend(destination, notification);
      log.info("[NOTI] Sent real-time notification to {} for user {}",
          destination, recipient.getUserId());
    } catch (Exception e) {
      log.error("[NOTI] Failed to send real-time notification to user {}: {}",
          recipient.getUserId(), e.getMessage(), e);
    }
  }

  @Override
  public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        .map(this::toResponse);
  }

  @Override
  public Page<NotificationResponse> getNotificationsByReadStatus(
      Long userId, Boolean isRead, Pageable pageable
  ) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, isRead, pageable)
        .map(this::toResponse);
  }

  @Override
  public long getUnreadCount(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return notificationRepository.countByUserAndIsRead(user, false);
  }

  @Override
  @Transactional
  public int markAllAsRead(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    int count = notificationRepository.markAllAsRead(user);
    log.info("[NOTI] Marked {} notifications as read for user {}", count, userId);
    return count;
  }

  @Override
  @Transactional
  public Optional<NotificationResponse> markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

    if (!notification.getUser().getUserId().equals(userId)) {
      throw new IllegalArgumentException("Notification does not belong to user");
    }

    notification.setIsRead(true);
    Notification saved = notificationRepository.save(notification);
    return Optional.of(toResponse(saved));
  }

  @Override
  public Page<NotificationResponse> getRecentNotifications(Long userId, int limit) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return notificationRepository.findByUserOrderByCreatedAtDesc(user, Pageable.ofSize(limit))
        .map(this::toResponse);
  }

  // ==================== Notification Settings ====================

  @Override
  public List<NotificationSettingResponse> getNotificationSettings(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    List<NotificationPreference> preferences = notificationPreferenceRepository.findByUserUserId(userId);

    // Convert existing preferences to response
    Map<NotificationType, NotificationSettingResponse> responseMap = preferences.stream()
        .collect(Collectors.toMap(
            NotificationPreference::getNotificationType,
            pref -> NotificationSettingResponse.builder()
                .prefId(pref.getPrefId())
                .type(pref.getNotificationType())
                .enabled(pref.getInApp())
                .build()
        ));

    // Return all notification types, default to enabled if not set
    return Arrays.stream(NotificationType.values())
        .map(type -> responseMap.computeIfAbsent(type, t ->
            NotificationSettingResponse.builder()
                .type(t)
                .enabled(true) // Default enabled
                .build()
        ))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public NotificationSettingResponse updateNotificationSetting(Long userId, NotificationType type, Boolean enabled) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    NotificationPreference preference = notificationPreferenceRepository
        .findByUserUserIdAndNotificationType(userId, type)
        .orElseGet(() -> NotificationPreference.builder()
            .user(user)
            .notificationType(type)
            .build());

    preference.setInApp(enabled);
    NotificationPreference saved = notificationPreferenceRepository.save(preference);

    log.info("[NOTI-SETTINGS] Updated notification setting for user {}: type={}, enabled={}",
        userId, type, enabled);

    return NotificationSettingResponse.builder()
        .prefId(saved.getPrefId())
        .type(saved.getNotificationType())
        .enabled(saved.getInApp())
        .build();
  }

  @Override
  @Transactional
  public List<NotificationSettingResponse> updateBatchNotificationSettings(
      Long userId, Map<NotificationType, Boolean> settings
  ) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    List<NotificationSettingResponse> results = new ArrayList<>();

    for (Map.Entry<NotificationType, Boolean> entry : settings.entrySet()) {
      NotificationPreference preference = notificationPreferenceRepository
          .findByUserUserIdAndNotificationType(userId, entry.getKey())
          .orElseGet(() -> NotificationPreference.builder()
              .user(user)
              .notificationType(entry.getKey())
              .build());

      preference.setInApp(entry.getValue());
      NotificationPreference saved = notificationPreferenceRepository.save(preference);

      results.add(NotificationSettingResponse.builder()
          .prefId(saved.getPrefId())
          .type(saved.getNotificationType())
          .enabled(saved.getInApp())
          .build());
    }

    log.info("[NOTI-SETTINGS] Batch updated {} notification settings for user {}",
        settings.size(), userId);

    return results;
  }

  private NotificationResponse toResponse(Notification notification) {
    NotificationResponse.ActorInfo actorInfo = null;
    if (notification.getActor() != null) {
      actorInfo = NotificationResponse.ActorInfo.builder()
          .actorId(notification.getActor().getUserId())
          .actorName(notification.getActor().getFullName())
          .actorAvatar(notification.getActor().getAvatarUrl())
          .build();
    }

    return NotificationResponse.builder()
        .notificationId(notification.getNotificationId())
        .type(notification.getType())
        .title(notification.getTitle())
        .content(notification.getContent())
        .targetUrl(notification.getTargetUrl())
        .data(notification.getData())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .actor(actorInfo)
        .build();
  }
}

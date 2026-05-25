package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.Notification.NotificationResponse;
import com.quocchung.cntt1.techcycle_system.model.Notification;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface NotificationService {

  NotificationResponse createNotification(
      User recipient,
      User actor,
      NotificationType type,
      String title,
      String content,
      String targetUrl,
      Map<String, Object> data
  );

  void sendRealTimeNotification(User recipient, NotificationResponse notification);

  Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);

  Page<NotificationResponse> getNotificationsByReadStatus(Long userId, Boolean isRead, Pageable pageable);

  long getUnreadCount(Long userId);

  int markAllAsRead(Long userId);

  Optional<NotificationResponse> markAsRead(Long notificationId, Long userId);

  Page<NotificationResponse> getRecentNotifications(Long userId, int limit);
}

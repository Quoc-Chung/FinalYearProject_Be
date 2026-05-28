package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Notification;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.utils.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  Page<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead, Pageable pageable);

  long countByUserAndIsRead(User user, Boolean isRead);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
  int markAllAsRead(@Param("user") User user);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
  int markAsRead(@Param("notificationId") Long notificationId);

  @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC LIMIT :limit")
  List<Notification> findTopNByUser(@Param("user") User user, @Param("limit") int limit);

  List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);
}

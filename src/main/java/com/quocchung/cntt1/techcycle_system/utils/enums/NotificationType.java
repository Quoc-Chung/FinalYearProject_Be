package com.quocchung.cntt1.techcycle_system.utils.enums;

public enum NotificationType {
  POST_PENDING, // Bài viết mới cần duyệt
  POST_APPROVED, //  /post/{postId}
  POST_REJECTED, //  /post/{postId}

  POST_REACTED,    // post/{postId}
  POST_COMMENTED,  // /post/{postId}
  COMMENT_REACTED, // /post/{postId}

  POST_REVIEW, // gọi khi ai đánh giá bai viết của bạn /post/{postId}

  USER_FOLLOWED,  //  /profile/{userId} với hàm

  USER_REGISTER, // Người dùng mới đăng ký

  CHAT_MESSAGE, // Có  tin nhắn mới lưu /messages Nhớ chuyển đường dẫn và truyền thêm ở data ?conversation={id} để nó tự động fin vào cuộc trò chuyện

  POST_REPORT, // Báo cáo bài viết vi phạm
  USER_REPORT, // Báo cáo người dùng vi phạm
  ABUSE_DETECTED, // Phát hiện nội dung bất thường (AI)
  SYSTEM, // Thông báo hệ thống
  TRANSACTION_COMPLETED // Giao dịch hoàn tất - click để đánh giá người bán/người mua
}
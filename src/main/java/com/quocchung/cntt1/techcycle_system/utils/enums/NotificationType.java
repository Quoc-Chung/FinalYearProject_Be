package com.quocchung.cntt1.techcycle_system.utils.enums;

public enum NotificationType {
  POST_APPROVED, //  /post/{postId}
  POST_REJECTED, //  /post/{postId}

  POST_REACTED,    // post/{postId}
  POST_COMMENTED,  // /post/{postId}
  COMMENT_REACTED, // /post/{postId}

  POST_REVIEW, // gọi khi ai đánh giá bai viết của bạn /post/{postId}

  USER_FOLLOWED,  //  /profile/{userId} với hàm 

  CHAT_MESSAGE // Có  tin nhắn mới lưu /messages Nhớ chuyển đường dẫn và truyền thêm ở data ?conversation={id} để nó tự động fin vào cuộc trò chuyện
}
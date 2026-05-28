package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Post.PresignedUrlResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller xử lý các API tải lên file cho chức năng chat.
 * Chỉ cung cấp endpoint tạo presigned URL để upload trực tiếp từ FE lên MinIO.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/uploads")
@RequiredArgsConstructor
public class ChatUploadController {

  private final MinIoService minIoService;
  private final ResponseUtils responseUtils;

  /**
   * Tạo presigned URL để tải file trực tiếp lên MinIO mà không cần qua server.
   * Cho phép upload file lớn hoặc nhiều file mà không tốn băng thông server.
   * FE sẽ sử dụng presigned URL này để upload trực tiếp lên MinIO.
   * @param fileName      Tên file cần tải lên
   * @param mimeType      Loại MIME của file (ví dụ: image/jpeg, audio/mp3)
   * @param type          Loại file: images, audio, hoặc files
   * @param userPrincipal Thông tin người dùng đã xác thực từ Spring Security
   * @return Presigned URL có thời hạn để upload trực tiếp và objectKey của file
   */
  @GetMapping("/presigned-url")
  public ResponseEntity<APIResponse<PresignedUrlResponse>> getPresignedUrl(
      @RequestParam String fileName,
      @RequestParam String mimeType,
      @RequestParam String type,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    String objectKey = generateChatObjectKey(userPrincipal.getUserId(), type, getExtension(fileName));
    PresignedUrlResponse presignedResponse = minIoService.generatePresignedPutUrl(objectKey, mimeType);

    PresignedUrlResponse response = new PresignedUrlResponse();
    response.setObjectKey(objectKey);
    response.setPresignedUrl(presignedResponse.getPresignedUrl());

    return ResponseEntity.ok(responseUtils.success(response));
  }

  /**
   * Tạo đường dẫn object key cho file trong MinIO theo cấu trúc: chat/{userId}/{type}/{uuid}.{extension}
   */
  private String generateChatObjectKey(Long userId, String type, String extension) {
    return String.format("chat/%d/%s/%s.%s", userId, type, UUID.randomUUID(), extension);
  }
  private String getExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".") + 1);
  }

}

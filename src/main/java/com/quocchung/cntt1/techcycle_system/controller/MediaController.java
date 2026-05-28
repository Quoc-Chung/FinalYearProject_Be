package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Post.PresignedUrlResponse;
import com.quocchung.cntt1.techcycle_system.security.UserPrincipal;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {
  private final MinIoService minIoService;
  private final ResponseUtils responseUtils;

  @GetMapping("/presigned-url")
  public ResponseEntity<APIResponse<PresignedUrlResponse>> getPresignedUrl(
      @RequestParam String fileName,
      @RequestParam String mimeType,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    String prefix = mimeType.toLowerCase().startsWith("video/")
        ? "PostMedia/video/" : "PostMedia/image/";
    String objectKey = prefix
                       + userPrincipal.getUserId() + "/"
                       + System.currentTimeMillis()
                       + "-" + UUID.randomUUID()
                       + getExtension(fileName, mimeType);

    PresignedUrlResponse response = minIoService.generatePresignedPutUrl(objectKey, mimeType);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  private String getExtension(String fileName, String mimeType) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }
    return switch (mimeType.toLowerCase()) {
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      case "video/mp4" -> ".mp4";
      case "video/webm" -> ".webm";
      case "video/quicktime" -> ".mov";
      default -> ".jpg";
    };
  }

  @GetMapping("/comment/presigned-url")
  public ResponseEntity<APIResponse<PresignedUrlResponse>> getCommentPresignedUrl(
      @RequestParam String fileName,
      @RequestParam String mimeType,
      @RequestParam Long commentId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    String prefix = mimeType.toLowerCase().startsWith("video/")
        ? "CommentMedia/video/" : "CommentMedia/image/";

    String objectKey = prefix
                       + userPrincipal.getUserId() + "/"
                       + commentId + "/"
                       + System.currentTimeMillis() + "-" + UUID.randomUUID()
                       + getExtension(fileName, mimeType);

    PresignedUrlResponse response = minIoService.generatePresignedPutUrl(objectKey, mimeType);
    return ResponseEntity.ok(responseUtils.success(response));
  }
}


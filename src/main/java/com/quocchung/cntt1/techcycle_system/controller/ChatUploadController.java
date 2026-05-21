package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Post.PresignedUrlResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat/uploads")
@RequiredArgsConstructor
public class ChatUploadController {

  private final MinIoService minIoService;
  private final UserRepository userRepository;
  private final ResponseUtils responseUtils;

  private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
  private static final long MAX_AUDIO_SIZE = 5 * 1024 * 1024;
  private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<ChatFileUploadResponse>> uploadImages(
      @RequestParam("files") MultipartFile[] files,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    List<ChatFileUploadResponse> responses = new ArrayList<>();

    for (MultipartFile file : files) {
      validateFile(file, MAX_IMAGE_SIZE, List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
      String objectKey = generateChatObjectKey(user.getUserId(), "images", getExtension(file.getOriginalFilename()));
      StorageUploadResponse uploadResponse = minIoService.uploadPostMedia(file, user.getUserId());
      responses.add(ChatFileUploadResponse.fromStorageResponse(objectKey, uploadResponse, "image"));
    }

    return ResponseEntity.ok(responseUtils.successList(responses));
  }

  @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<ChatFileUploadResponse>> uploadAudio(
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    validateFile(file, MAX_AUDIO_SIZE, List.of("audio/mpeg", "audio/mp4", "audio/webm", "audio/ogg", "audio/wav"));
    String objectKey = generateChatObjectKey(user.getUserId(), "audio", getExtension(file.getOriginalFilename()));
    StorageUploadResponse uploadResponse = minIoService.uploadPostMedia(file, user.getUserId());
    ChatFileUploadResponse response = ChatFileUploadResponse.fromStorageResponse(objectKey, uploadResponse, "audio");

    return ResponseEntity.ok(responseUtils.success(response));
  }

  @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<ChatFileUploadResponse>> uploadFiles(
      @RequestParam("files") MultipartFile[] files,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    List<ChatFileUploadResponse> responses = new ArrayList<>();

    for (MultipartFile file : files) {
      validateFile(file, MAX_FILE_SIZE, List.of(
          "application/pdf", "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "text/plain", "application/zip"
      ));
      String objectKey = generateChatObjectKey(user.getUserId(), "files", getExtension(file.getOriginalFilename()));
      StorageUploadResponse uploadResponse = minIoService.uploadPostMedia(file, user.getUserId());
      responses.add(ChatFileUploadResponse.fromStorageResponse(objectKey, uploadResponse, "file"));
    }

    return ResponseEntity.ok(responseUtils.successList(responses));
  }

  @GetMapping("/presigned-url")
  public ResponseEntity<APIResponse<PresignedUrlResponse>> getPresignedUrl(
      @RequestParam String fileName,
      @RequestParam String mimeType,
      @RequestParam String type,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = getUser(userDetails);
    String objectKey = generateChatObjectKey(user.getUserId(), type, getExtension(fileName));
    PresignedUrlResponse presignedResponse = minIoService.generatePresignedPutUrl(objectKey, mimeType);

    PresignedUrlResponse response = new PresignedUrlResponse();
    response.setObjectKey(objectKey);
    response.setPresignedUrl(presignedResponse.getPresignedUrl());

    return ResponseEntity.ok(responseUtils.success(response));
  }

  private void validateFile(MultipartFile file, long maxSize, List<String> allowedTypes) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    if (file.getSize() > maxSize) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size");
    }
    if (!allowedTypes.contains(file.getContentType())) {
      throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
    }
  }

  private String generateChatObjectKey(Long userId, String type, String extension) {
    return String.format("chat/%d/%s/%s.%s", userId, type, UUID.randomUUID(), extension);
  }

  private String getExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".") + 1);
  }

  private User getUser(UserDetails userDetails) {
    return userRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public record ChatFileUploadResponse(
      String objectKey,
      String url,
      String type,
      Long fileSize,
      String mimeType
  ) {
    public static ChatFileUploadResponse fromStorageResponse(String objectKey, StorageUploadResponse response, String type) {
      return new ChatFileUploadResponse(
          objectKey,
          response.getUrl(),
          type,
          response.getFileSize(),
          response.getMimeType()
      );
    }
  }
}

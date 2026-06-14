package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.config.MinioProperties;
import com.quocchung.cntt1.techcycle_system.dtos.request.Post.PresignedUrlResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.utils.enums.MediaType;
import io.minio.BucketExistsArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnBean(MinioClient.class)
@Slf4j
@RequiredArgsConstructor
public class MinIoServiceImpl implements MinIoService {

  private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
  private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png",
      "image/webp");
  private static final Set<String> ALLOWED_POST_IMAGE_CONTENT_TYPES = Set.of("image/jpeg",
      "image/png", "image/webp");
  private static final Set<String> ALLOWED_POST_VIDEO_CONTENT_TYPES = Set.of("video/mp4",
      "video/webm", "video/quicktime");

  private static final Set<String> ALLOWED_PRESIGNED_MIME_TYPES = Set.of(
      "image/jpeg", "image/png", "image/webp",
      "video/mp4", "video/webm", "video/quicktime"
  );

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;
  @Value("${minio.post-image-max-size-bytes:10485760}")
  private long postImageMaxSizeBytes;
  @Value("${minio.post-video-max-size-bytes:104857600}")
  private long postVideoMaxSizeBytes;
  @PostConstruct
  public void initBucket() {
    try {
      boolean exists = minioClient.bucketExists(
          BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build()
      );
      if (!exists) {
        minioClient.makeBucket(
            MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
      }
      setBucketPolicyPublic();
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot initialize MinIO bucket", ex);
    }
  }

  @Override
  public void setBucketPolicyPublic() {
    try {
      String bucketName = minioProperties.getBucketName();
      String policyJson = """
          {
            "Version": "2012-10-17",
            "Statement": [
              {
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }
            ]
          }
          """.formatted(bucketName);

      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder()
              .bucket(bucketName)
              .config(policyJson)
              .build()
      );
      log.info("Bucket policy set to public for bucket: {}", bucketName);
    } catch (Exception ex) {
      log.warn("Cannot set bucket policy to public: {}", ex.getMessage());
    }
  }


  @Override
  public StorageUploadResponse uploadUserAvatar(MultipartFile file, Long userId) {
    validateAvatar(file);
    String extension = getExtension(file.getOriginalFilename(), file.getContentType());
    String objectKey =
        "UserAvatar/" + userId + "/" + System.currentTimeMillis() + "-" + UUID.randomUUID()
        + extension;

    return uploadObject(file, objectKey, MediaType.IMAGE);
  }

  @Override
  public StorageUploadResponse uploadPostMedia(MultipartFile file, Long userId) {
    MediaType mediaType = detectMediaType(file.getContentType());
    validatePostMedia(file, mediaType);
    String extension = getExtension(file.getOriginalFilename(), file.getContentType());
    String folder = mediaType == MediaType.VIDEO ? "PostMedia/video/" : "PostMedia/image/";
    String objectKey = folder + userId + "/" + System.currentTimeMillis() + "-" + UUID.randomUUID()
        + extension;

    return uploadObject(file, objectKey, mediaType);
  }

  @Override
  public StorageUploadResponse uploadBrandLogo(MultipartFile file, Long brandId) {
    validateAvatar(file);
    String extension = getExtension(file.getOriginalFilename(), file.getContentType());
    String objectKey = "BrandLogo/" + brandId + "/" + System.currentTimeMillis() + "-"
        + UUID.randomUUID() + extension;
    return uploadObject(file, objectKey, MediaType.IMAGE);
  }

  @Override
  public StorageUploadResponse uploadCategoryIcon(MultipartFile file, Long categoryId) {
    validateAvatar(file);
    String extension = getExtension(file.getOriginalFilename(), file.getContentType());
    String objectKey = "CategoryIcon/" + categoryId + "/" + System.currentTimeMillis() + "-"
        + UUID.randomUUID() + extension;
    return uploadObject(file, objectKey, MediaType.IMAGE);
  }

  private StorageUploadResponse uploadObject(MultipartFile file, String objectKey, MediaType mediaType) {
    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(minioProperties.getBucketName())
              .object(objectKey)
              .stream(inputStream, file.getSize(), -1)
              .contentType(file.getContentType())
              .build()
      );
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.GENERAL_ERROR, "Upload avatar failed");
    }

    return StorageUploadResponse.builder()
        .objectKey(objectKey)
        .url(buildObjectUrl(objectKey))
        .mediaType(mediaType)
        .mimeType(file.getContentType())
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .build();
  }

  @Override
  public void deleteObjectSilently(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return;
    }
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder()
              .bucket(minioProperties.getBucketName())
              .object(objectKey)
              .build()
      );
    } catch (Exception ignored) {
    }
  }

  private String buildObjectUrl(String objectKey) {
    String base = minioProperties.getPublicEndpoint();
    if (base == null || base.isBlank()) {
      base = minioProperties.getEndpoint();
    }
    String normalized = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    return normalized + "/" + minioProperties.getBucketName() + "/" + objectKey;
  }

  private void validateAvatar(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "avatar", "Avatar file is empty");
    }
    if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "avatar", "Avatar file must be <= 5MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "avatar", "Unsupported file type");
    }
  }

  private void validatePostMedia(MultipartFile file, MediaType mediaType) {
    if (file == null || file.isEmpty()) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "media", "Media file is empty");
    }
    String contentType = normalizeContentType(file.getContentType());
    if (mediaType == MediaType.IMAGE) {
      if (!ALLOWED_POST_IMAGE_CONTENT_TYPES.contains(contentType)) {
        throw new ResException(ResErrorCode.BAD_REQUEST, "media", "Unsupported image type");
      }
      if (file.getSize() > postImageMaxSizeBytes) {
        throw new ResException(ResErrorCode.BAD_REQUEST, "media",
            "Image file exceeds allowed limit");
      }
      return;
    }

    if (!ALLOWED_POST_VIDEO_CONTENT_TYPES.contains(contentType)) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "media", "Unsupported video type");
    }
    if (file.getSize() > postVideoMaxSizeBytes) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "media", "Video file exceeds allowed limit");
    }
  }

  private MediaType detectMediaType(String contentType) {
    String normalized = normalizeContentType(contentType);
    if (normalized.startsWith("image/")) {
      return MediaType.IMAGE;
    }
    if (normalized.startsWith("video/")) {
      return MediaType.VIDEO;
    }
    throw new ResException(ResErrorCode.BAD_REQUEST, "media", "Unsupported media type");
  }

  private String normalizeContentType(String contentType) {
    return contentType == null ? "" : contentType.toLowerCase();
  }

  private String getExtension(String filename, String contentType) {
    if (filename != null && filename.contains(".")) {
      return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
    if ("image/png".equalsIgnoreCase(contentType)) {
      return ".png";
    }
    if ("image/webp".equalsIgnoreCase(contentType)) {
      return ".webp";
    }
    if ("video/mp4".equalsIgnoreCase(contentType)) {
      return ".mp4";
    }
    if ("video/webm".equalsIgnoreCase(contentType)) {
      return ".webm";
    }
    if ("video/quicktime".equalsIgnoreCase(contentType)) {
      return ".mov";
    }
    return ".jpg";
  }
  @Override
  public PresignedUrlResponse generatePresignedPutUrl(String objectKey, String mimeType) {
    if (mimeType == null || !ALLOWED_PRESIGNED_MIME_TYPES.contains(mimeType.toLowerCase())) {
      throw new ResException(ResErrorCode.BAD_REQUEST, "mimeType",
          "Unsupported media type: " + mimeType);
    }
    try {
      MinioClient publicMinioClient = MinioClient.builder()
          .endpoint(minioProperties.getPublicEndpoint())
          .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
          .region("us-east-1")
          .build();

      String presignedUrl = publicMinioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .bucket(minioProperties.getBucketName())
              .object(objectKey)
              .method(Method.PUT)
              .expiry(15, TimeUnit.MINUTES)
              .extraQueryParams(Map.of("Content-Type", mimeType))
              .build()
      );

      return PresignedUrlResponse.builder()
          .presignedUrl(presignedUrl)
          .objectKey(objectKey)
          .publicUrl(buildObjectUrl(objectKey))
          .expiresInSeconds(900L)
          .build();

    } catch (ResException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResException(ResErrorCode.GENERAL_ERROR,
          "Cannot generate presigned URL: " + ex.getMessage());
    }
  }
}

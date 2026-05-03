package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MinIoService {

  StorageUploadResponse uploadUserAvatar(MultipartFile file, Long userId);

  StorageUploadResponse uploadPostMedia(MultipartFile file, Long userId);

  StorageUploadResponse uploadBrandLogo(MultipartFile file, Long brandId);

  StorageUploadResponse uploadCategoryIcon(MultipartFile file, Long categoryId);

  void deleteObjectSilently(String objectKey);

  void setBucketPolicyPublic();
}

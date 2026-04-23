package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.User.UserRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.model.UserImage;
import com.quocchung.cntt1.techcycle_system.repository.UserImageRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import com.quocchung.cntt1.techcycle_system.service.UserService;
import com.quocchung.cntt1.techcycle_system.utils.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserImageRepository userImageRepository;
  private final MinIoService minioService;
  private final Converter converter;

  @Override
  @Transactional
  public UserResponse updateMe(String email, UserRequest request) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "User not found"));

    updateBasicInfo(user, request);
    handleAvatar(user, request.getAvatar());

    User saved = userRepository.save(user);
    return converter.mapResponse(saved);
  }

  private void updateBasicInfo(User user, UserRequest request) {
    if (request.getFullName() != null && !request.getFullName().isBlank()) {
      user.setFullName(request.getFullName().trim());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone().trim());
    }
    if (request.getBio() != null) {
      user.setBio(request.getBio().trim());
    }
  }

  private void handleAvatar(User user, MultipartFile avatarFile) {
    if (avatarFile == null || avatarFile.isEmpty()) {
      return;
    }

    UserImage currentAvatar = userImageRepository.findFirstByUserUserIdAndIsAvatarTrue(
        user.getUserId()).orElse(null);
    String oldObjectKey = currentAvatar != null ? currentAvatar.getObjectKey() : null;

    StorageUploadResponse uploadResult = minioService.uploadUserAvatar(avatarFile,
        user.getUserId());
    user.setAvatarUrl(uploadResult.getUrl());

    UserImage avatarRow = currentAvatar != null ? currentAvatar : UserImage.builder()
        .user(user)
        .isAvatar(true)
        .build();

    avatarRow.setObjectKey(uploadResult.getObjectKey());
    avatarRow.setImageUrl(uploadResult.getUrl());
    userImageRepository.save(avatarRow);

    if (oldObjectKey != null && !oldObjectKey.equals(uploadResult.getObjectKey())) {
      minioService.deleteObjectSilently(oldObjectKey);
    }
  }
}

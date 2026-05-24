package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.UserImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Long> {
  Optional<UserImage> findFirstByUserUserIdAndIsAvatarTrue(Long userId);

  @Query("SELECT ui FROM UserImage ui WHERE ui.user.userId = :userId ORDER BY ui.createdAt DESC")
  List<UserImage> findAllByUserId(@Param("userId") Long userId);
}

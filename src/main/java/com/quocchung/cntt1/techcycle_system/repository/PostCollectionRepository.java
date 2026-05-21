package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostCollection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCollectionRepository extends JpaRepository<PostCollection, Long> {

  @Query("SELECT pc FROM PostCollection pc WHERE pc.user.userId = :userId")
  List<PostCollection> findAllByUserUserId(Long userId);

  @Query("SELECT COUNT(pc) > 0 FROM PostCollection pc WHERE pc.user.userId = :userId AND pc.name = :name")
  boolean existsByUserUserIdAndName(Long userId, String name);

  @Query("SELECT COUNT(pc) > 0 FROM PostCollection pc WHERE pc.user.userId = :userId AND pc.name = :name AND pc.collectionId <> :collectionId")
  boolean existsByUserUserIdAndNameAndCollectionIdNot(Long userId, String name, Long collectionId);
}

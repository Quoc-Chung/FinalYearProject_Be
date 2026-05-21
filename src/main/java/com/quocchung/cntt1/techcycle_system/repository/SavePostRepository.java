package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.SavedPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SavePostRepository extends JpaRepository<SavedPost, Long> {
  @Modifying
  @Transactional
  @Query("DELETE FROM SavedPost sp WHERE sp.collection.collectionId = :collectionId")
  void deleteAllByCollectionCollectionId(@Param("collectionId") Long collectionId);


  boolean existsByUserUserIdAndPostPostIdAndCollectionCollectionId(
      Long userId, Long postId, Long collectionId);

  Optional<SavedPost> findByUserUserIdAndPostPostIdAndCollectionCollectionId(
      Long userId, Long postId, Long collectionId);

  List<SavedPost> findAllByUserUserIdAndCollectionCollectionId(
      Long userId, Long collectionId);
}

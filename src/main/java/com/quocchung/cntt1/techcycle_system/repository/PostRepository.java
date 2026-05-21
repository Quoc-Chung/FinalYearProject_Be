package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

  Optional<Post> findById(Long id);

  Page<Post> findAll(Pageable pageable);

  Page<Post> findByUserUserId(Long userId, Pageable pageable);

  List<Post> findByUserUserId(Long userId);

  long countByUserUserId(Long userId);


  List<Post> findByStatusOrderByCreatedAtDesc(PostStatus status);

  List<Post> findByUserUserIdAndStatus(Long userId, PostStatus status);

}

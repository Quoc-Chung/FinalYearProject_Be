package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.PostView;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

  @Query("SELECT COUNT(pv) FROM PostView pv WHERE pv.viewedAt >= :since")
  long countViewsSince(@Param("since") LocalDateTime since);

  @Query("SELECT pv.post.postId, COUNT(pv) FROM PostView pv WHERE pv.post.postId IN :postIds GROUP BY pv.post.postId")
  List<Object[]> countViewsByPostIds(@Param("postIds") List<Long> postIds);
}

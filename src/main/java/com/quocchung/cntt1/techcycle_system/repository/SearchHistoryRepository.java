package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.SearchHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
  @Query("""
        SELECT sh.keyword FROM SearchHistory sh
        WHERE sh.user.userId = :userId
        GROUP BY sh.keyword
        ORDER BY MAX(sh.createdAt) DESC
        LIMIT :limit
    """)
  List<String> findRecentKeywordsByUserId(@Param("userId") Long userId,
      @Param("limit") int limit);

  void deleteAllByUserUserId(Long userId);

}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Category;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByNameContainingIgnoreCase(String keyword, Sort sort);

  List<Category> findByParentIsNull(Sort sort);

  List<Category> findByParentIsNullAndIsActiveTrue(Sort sort);

  Page<Category> findAll(Pageable pageable);

  List<Category> findByParent_CategoryId(Long parentId);

  @Query("SELECT c FROM Category c WHERE " +
         "(:searchText IS NULL OR :searchText = '' OR " +
         "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
  Page<Category> findBySearchText(@Param("searchText") String searchText, Pageable pageable);
}

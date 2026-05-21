package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Brand;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
  List<Brand> findByNameContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
      String keywordByName,
      String keywordByCategory,
      Sort sort
  );

  Page<Brand> findAll(Pageable pageable);

  @Query("SELECT b FROM Brand b WHERE " +
         "(:searchText IS NULL OR :searchText = '' OR " +
         "LOWER(b.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
         "LOWER(b.category.name) LIKE LOWER(CONCAT('%', :searchText, '%')))")
  Page<Brand> findBySearchText(@Param("searchText") String searchText, Pageable pageable);
}

package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Brand;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
  List<Brand> findByNameContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
      String keywordByName,
      String keywordByCategory,
      Sort sort
  );
}

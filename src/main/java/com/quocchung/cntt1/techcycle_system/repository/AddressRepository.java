package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

  List<Address> findByUserId(Long userId);

  Optional<Address> findFirstByUserIdAndIsDefaultTrue(Long userId);
}

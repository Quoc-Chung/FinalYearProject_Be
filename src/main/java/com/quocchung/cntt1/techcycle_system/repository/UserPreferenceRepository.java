package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
  
  Optional<UserPreference> findByUserUserId(Long userId);
  
  boolean existsByUserUserId(Long userId);
}

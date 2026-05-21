package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("UPDATE User u SET u.isFirstLogin = false WHERE u.userId = :id AND u.isFirstLogin = true")
  int markFirstLoginDone(@Param("id") Long id);

  @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON ur.user = u JOIN Role r ON ur.role = r WHERE r.name = :roleName")
  List<User> findAllByRoleName(@Param("roleName") String roleName);

  
}
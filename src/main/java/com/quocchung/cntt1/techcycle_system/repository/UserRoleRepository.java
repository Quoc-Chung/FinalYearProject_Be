package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
  boolean existsByUserUserIdAndRoleRoleId(Long userId, Integer roleId);

  @Query("select ur.role.name from UserRole ur where ur.user.userId = :userId")
  List<String> findRoleNamesByUserId(@Param("userId") Long userId);

  @Query("select ur.role.roleId from UserRole ur where ur.user.userId = :userId")
  List<Integer> findRoleIdsByUserId(@Param("userId") Long userId);


  @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN TRUE ELSE FALSE END " +
         "FROM UserRole ur " +
         "WHERE ur.user.userId = :userId AND ur.role.name = :roleName")
  Boolean checkUserIsAdmin(@Param("userId") Long userId,
      @Param("roleName") String roleName);
}

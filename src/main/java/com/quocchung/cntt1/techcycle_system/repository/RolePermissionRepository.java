package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

  @Query("""
      select distinct concat(rp.permission.resource, ':', str(rp.permission.action))
      from RolePermission rp
      where rp.role.roleId in :roleIds
      """)
  List<String> findPermissionAuthoritiesByRoleIds(@Param("roleIds") List<Integer> roleIds);
}

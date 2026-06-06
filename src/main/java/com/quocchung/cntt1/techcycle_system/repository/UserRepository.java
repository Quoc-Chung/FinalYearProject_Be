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


  @Query("SELECT DISTINCT u FROM User u WHERE u.userId IN " +
         "(SELECT ur.user.userId FROM UserRole ur WHERE ur.role.name = :roleName)")
  List<User> findAllByRoleName(@Param("roleName") String roleName);

  @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRole ur " +
         "JOIN Role r ON ur.role = r WHERE ur.user.userId = :userId AND r.name = :roleName")
  boolean hasRole(@Param("userId") Long userId, @Param("roleName") String roleName);

  @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
  List<User> findRecentUsers(org.springframework.data.domain.Pageable pageable);

  @Query(
      "SELECT u FROM User u "
      + "INNER JOIN UserRole ur ON ur.user.userId = u.userId "
      + "INNER JOIN ur.role "
      + "WHERE ur.role.name = 'ADMIN'"
  )
  List<User> getAllAdmin();

  @Query(value = "SELECT u.* FROM users u " +
         "LEFT JOIN posts p ON u.user_id = p.user_id AND p.delete_at IS NULL " +
         "WHERE u.deleted_at IS NULL AND u.status = 'ACTIVE' " +
         "GROUP BY u.user_id " +
         "ORDER BY COUNT(p.post_id) DESC",
         countQuery = "SELECT COUNT(DISTINCT u.user_id) FROM users u WHERE u.deleted_at IS NULL AND u.status = 'ACTIVE'",
         nativeQuery = true)
  List<User> findTopSellersByPostCount(org.springframework.data.domain.Pageable pageable);

  @Query("""
        SELECT p.user
        FROM Post p
        WHERE p.postId = :postId
    """)
  Optional<User> getUserByPostId(@Param("postId") Long postId);

}
package com.quocchung.cntt1.techcycle_system.security;

import com.quocchung.cntt1.techcycle_system.model.User;
import com.quocchung.cntt1.techcycle_system.repository.RolePermissionRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRepository;
import com.quocchung.cntt1.techcycle_system.repository.UserRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Set<GrantedAuthority> authorities = buildAuthorities(user.getUserId());
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .authorities(authorities)
        .build();
  }

  public Set<GrantedAuthority> buildAuthorities(Long userId) {
    List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
    List<Integer> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
    List<String> permissions = roleIds.isEmpty()
        ? List.of()
        : rolePermissionRepository.findPermissionAuthoritiesByRoleIds(roleIds);

    Set<GrantedAuthority> authorities = new HashSet<>();
    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
    permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.toUpperCase())));
    return authorities;
  }
}

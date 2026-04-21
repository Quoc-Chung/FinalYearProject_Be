package com.quocchung.cntt1.techcycle_system.model;
import com.quocchung.cntt1.techcycle_system.utils.enums.Action;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"resource", "action"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer permissionId;

  // API path: /api/users
  @Column(nullable = false, length = 100)
  private String resource;

  // CREATE, READ, UPDATE, DELETE, ALL
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Action action;

  @Column(length = 255)
  private String description;
}
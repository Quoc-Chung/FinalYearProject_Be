package com.quocchung.cntt1.techcycle_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

  @Id
  @Column(name = "address_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long addressId;

  @Column(name = "user_id")
  private Long userId;

  @Column(nullable = false, length = 255)
  private String province;

  @Column(length = 100)
  private String ward;

  @Column(nullable = false, length = 1000)
  private String addressDetail; // đường nào, số nhà bao nhiêu

  @Column(name = "address_line", length = 5000)
  private String addressLine; // province - ward - addressDetail

  @Column(name = "is_default")
  @Builder.Default
  private Boolean isDefault = false;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;
}
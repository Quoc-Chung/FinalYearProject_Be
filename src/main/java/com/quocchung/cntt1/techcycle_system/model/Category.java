package com.quocchung.cntt1.techcycle_system.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long categoryId;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Category parent;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "icon_url")
  private String iconUrl;


  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

}
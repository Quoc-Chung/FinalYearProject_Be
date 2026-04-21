package com.quocchung.cntt1.techcycle_system.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "post_attributes",
    indexes = @Index(name = "idx_post_attribute", columnList = "post_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAttribute {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attribute_id", updatable = false, nullable = false)
  private Long attributeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(name = "attribute_key", nullable = false, length = 100)
  private String attributeKey;

  @Column(name = "attribute_value", length = 255)
  private String attributeValue;

  @Column(name = "sort_order")
  @Builder.Default
  private Integer sortOrder = 0;
}
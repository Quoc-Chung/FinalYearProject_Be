package com.quocchung.cntt1.techcycle_system.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(
    name = "post_tags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"}),
    indexes = {
        @Index(name = "idx_post_tag_post_id", columnList = "post_id"),
        @Index(name = "idx_post_tag_tag_id", columnList = "tag_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "tag_id", nullable = false)
  private Tag tag;
}
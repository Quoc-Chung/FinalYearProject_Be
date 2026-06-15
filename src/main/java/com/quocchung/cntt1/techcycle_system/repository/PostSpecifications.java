package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Address;
import com.quocchung.cntt1.techcycle_system.model.Post;
import com.quocchung.cntt1.techcycle_system.utils.enums.PostStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {

  public static Specification<Post> hasStatus(PostStatus status) {
    return (root, query, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("status"), status);
    };
  }

  public static Specification<Post> hasCategoryIds(List<Long> categoryIds) {
    return (root, query, cb) ->
        root.get("category").get("categoryId").in(categoryIds);
  }

  public static Specification<Post> hasKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + keyword.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), pattern),
          cb.like(cb.lower(root.get("description")), pattern)
      );
    };
  }

  public static Specification<Post> hasTitle(String title) {
    return (root, query, cb) -> {
      if (title == null || title.isBlank()) {
        return cb.conjunction();
      }
      return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasDescription(String description) {
    return (root, query, cb) -> {
      if (description == null || description.isBlank()) {
        return cb.conjunction();
      }
      return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasAuthorFullName(String authorName) {
    return (root, query, cb) -> {
      if (authorName == null || authorName.isBlank()) return cb.conjunction();
      Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
      return cb.like(cb.lower(userJoin.get("fullName")), "%" + authorName.toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasAddressLine(String address) {
    return (root, query, cb) -> {
      if (address == null || address.isBlank()) return cb.conjunction();
      Join<Post, Address> addressJoin = root.join("address", JoinType.LEFT);
      String pattern = "%" + address.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(addressJoin.get("addressLine")), pattern),
          cb.like(cb.lower(addressJoin.get("addressDetail")), pattern),
          cb.like(cb.lower(addressJoin.get("province")), pattern),
          cb.like(cb.lower(addressJoin.get("ward")), pattern)
      );
    };
  }

  public static Specification<Post> hasProvince(String province) {
    return (root, query, cb) -> {
      if (province == null || province.isBlank()) return cb.conjunction();
      Join<Object, Object> addressJoin = root.join("address", JoinType.LEFT);
      return cb.like(cb.lower(addressJoin.get("province")), "%" + province.toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasWard(String ward) {
    return (root, query, cb) -> {
      if (ward == null || ward.isBlank()) return cb.conjunction();
      Join<Object, Object> addressJoin = root.join("address", JoinType.LEFT);
      return cb.like(cb.lower(addressJoin.get("ward")), "%" + ward.toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasCategoryId(Long categoryId) {
    return (root, query, cb) -> {
      if (categoryId == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("category").get("categoryId"), categoryId);
    };
  }

  public static Specification<Post> hasBrandId(Long brandId) {
    return (root, query, cb) -> {
      if (brandId == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("brand").get("brandId"), brandId);
    };
  }

  public static Specification<Post> minPrice(Double minPrice) {
    return (root, query, cb) -> {
      if (minPrice == null) {
        return cb.conjunction();
      }
      return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    };
  }

  public static Specification<Post> maxPrice(Double maxPrice) {
    return (root, query, cb) -> {
      if (maxPrice == null) {
        return cb.conjunction();
      }
      return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    };
  }

  public static Specification<Post> hasTag(String tagName) {
    return (root, query, cb) -> {
      if (tagName == null || tagName.isBlank()) {
        return cb.conjunction();
      }
      query.distinct(true);

      Join<Object, Object> postTagsJoin = root.join("postTags");
      Join<Object, Object> tagJoin = postTagsJoin.join("tag");
      return cb.like(
          cb.lower(tagJoin.get("name")),
          "%" + tagName.toLowerCase() + "%"
      );
    };
  }

}

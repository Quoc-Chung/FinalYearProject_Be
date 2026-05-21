package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Category.CreateCategoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryTreeResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.mapper.CategoryMapper;
import com.quocchung.cntt1.techcycle_system.model.Category;
import com.quocchung.cntt1.techcycle_system.repository.CategoryRepository;
import com.quocchung.cntt1.techcycle_system.service.CategoryService;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;
  private final MinIoService minIoService;
  private final CategoryMapper categoryMapper;

  @Override
  @Transactional
  public CategoryResponse create(CreateCategoryRequest request, MultipartFile iconFile) {
    Category parent = resolveParent(request.getParentId());
    Category category = Category.builder()
        .name(request.getName().trim())
        .parent(parent)
        .isActive(request.getIsActive() == null || request.getIsActive())
        .build();
    category = categoryRepository.save(category);

    if (iconFile != null && !iconFile.isEmpty()) {
      StorageUploadResponse uploadResponse = minIoService.uploadCategoryIcon(iconFile,
          category.getCategoryId());
      category.setIconUrl(uploadResponse.getUrl());
      category = categoryRepository.save(category);
    }
    return mapResponse(category);
  }

  @Override
  @Transactional
  public CategoryResponse update(Long categoryId, CreateCategoryRequest request, MultipartFile iconFile) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Category not found"));

    category.setName(request.getName().trim());
    category.setIsActive(request.getIsActive() == null ? category.getIsActive() : request.getIsActive());
    category.setParent(resolveParent(request.getParentId()));

    if (iconFile != null && !iconFile.isEmpty()) {
      StorageUploadResponse uploadResponse = minIoService.uploadCategoryIcon(iconFile,
          category.getCategoryId());
      category.setIconUrl(uploadResponse.getUrl());
    }
    category = categoryRepository.save(category);
    return mapResponse(category);
  }

  @Override
  @Transactional
  public void delete(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Category not found"));
    categoryRepository.delete(category);
  }

  @Override
  public List<CategoryResponse> getAllData(String searchText) {
    Sort sort = Sort.by(Sort.Direction.DESC, "categoryId");
    List<Category> categories = (searchText == null || searchText.isBlank())
        ? categoryRepository.findAll(sort)
        : categoryRepository.findByNameContainingIgnoreCase(searchText.trim(), sort);
    return categories.stream().map(this::mapResponse).toList();
  }

  @Override
  public Page<CategoryResponse> getAllData(String searchText, Pageable pageable) {
    Page<Category> categoryPage = categoryRepository.findBySearchText(searchText, pageable);
    return categoryPage.map(this::mapResponse);
  }

  private Category resolveParent(Long parentId) {
    if (parentId == null) {
      return null;
    }
    return categoryRepository.findById(parentId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Parent category not found"));
  }

  private CategoryResponse mapResponse(Category category) {
    return CategoryResponse.builder()
        .categoryId(category.getCategoryId())
        .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
        .parentName(category.getParent() != null ? category.getParent().getName() : null)
        .name(category.getName())
        .iconUrl(category.getIconUrl())
        .isActive(category.getIsActive())
        .build();
  }

  @Override
  public List<CategoryTreeResponse> getAllTreeData(String searchText) {
    // Query flat list từ DB
    List<CategoryTreeResponse> flatList = categoryMapper.getAllCategories(
        (searchText == null || searchText.isBlank()) ? null : searchText.trim()
    );
    return buildTree(flatList);
  }

  @Override
  public List<CategoryResponse> getRootCategories() {
    Sort sort = Sort.by(Sort.Direction.ASC, "name");
    List<Category> categories = categoryRepository.findByParentIsNull(sort);
    return categories.stream().map(this::mapResponse).toList();
  }

  @Override
  public List<CategoryResponse> getRootCategoriesActive() {
    Sort sort = Sort.by(Sort.Direction.ASC, "name");
    List<Category> categories = categoryRepository.findByParentIsNullAndIsActiveTrue(sort);
    return categories.stream().map(this::mapResponse).toList();
  }
  private List<CategoryTreeResponse> buildTree(List<CategoryTreeResponse> flatList) {
    // Map theo categoryId để lookup nhanh
    Map<Long, CategoryTreeResponse> map = flatList.stream()
        .collect(Collectors.toMap(CategoryTreeResponse::getCategoryId, c -> {
          c.setChildrens(new ArrayList<>());
          return c;
        }));

    List<CategoryTreeResponse> roots = new ArrayList<>();

    for (CategoryTreeResponse node : flatList) {
      if (node.getParentId() == null) {
        roots.add(node);
      } else {
        CategoryTreeResponse parent = map.get(node.getParentId());
        if (parent != null) {
          parent.getChildrens().add(node);
        }
      }
    }

    return roots;
  }
}

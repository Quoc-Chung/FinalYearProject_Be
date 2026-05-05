package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Category.CreateCategoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryTreeResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {
  CategoryResponse create(CreateCategoryRequest request, MultipartFile iconFile);

  CategoryResponse update(Long categoryId, CreateCategoryRequest request, MultipartFile iconFile);

  void delete(Long categoryId);

  List<CategoryResponse> getAllData(String searchText);

  List<CategoryTreeResponse> getAllTreeData(String searchText);
}

package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Category.CreateCategoryRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryTreeResponse;
import com.quocchung.cntt1.techcycle_system.service.CategoryService;
import com.quocchung.cntt1.techcycle_system.utils.PageResponseUtil;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;
  private final ResponseUtils responseUtils;

  @GetMapping("/getAllData")
  public ResponseEntity<APIResponse<CategoryResponse>> getAllData(
      @RequestParam(name = "searchText", required = false) String searchText,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size
  ) {
    PageRequest pageable = PageRequest.of(page - 1, size);
    Page<CategoryResponse> categoryPage = categoryService.getAllData(searchText, pageable);
    PageResponse pageInfo = PageResponseUtil.extractPageMetadata(categoryPage);
    return ResponseEntity.ok(responseUtils.successPage(categoryPage.getContent(), pageInfo));
  }
  

  @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<CategoryResponse>> create(
      @Valid @ModelAttribute CreateCategoryRequest request,
      @RequestParam("logo") MultipartFile logo
  ) {
    CategoryResponse response = categoryService.create(request, logo);
    return ResponseEntity.ok(responseUtils.success(response));
  }


  @PutMapping(value = "/edit/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<CategoryResponse>> update(
      @PathVariable Long categoryId,
      @Valid @ModelAttribute CreateCategoryRequest request,
      @RequestParam(value = "logo", required = false) MultipartFile logo
  ) {
    CategoryResponse response = categoryService.update(categoryId, request, logo);
    return ResponseEntity.ok(responseUtils.success(response));
  }


  @DeleteMapping("/delete/{categoryId}")
  public ResponseEntity<APIResponse<String>> delete(@PathVariable Long categoryId) {
    categoryService.delete(categoryId);
    return ResponseEntity.ok(responseUtils.success("Delete category successfully"));
  }

  @GetMapping("/getAllTreeData")
  public ResponseEntity<APIResponse<CategoryTreeResponse>> getAllTreeData(
      @RequestParam(name = "searchText", required = false) String searchText) {
    return ResponseEntity.ok(responseUtils.successList(categoryService.getAllTreeData(searchText)));
  }

  @GetMapping("/root")
  public ResponseEntity<APIResponse<CategoryResponse>> getRootCategories() {
    return ResponseEntity.ok(responseUtils.successList(categoryService.getRootCategories()));
  }

  @GetMapping("/root/active")
  public ResponseEntity<APIResponse<CategoryResponse>> getRootCategoriesActive() {
    return ResponseEntity.ok(responseUtils.successList(categoryService.getRootCategoriesActive()));
  }
}

package com.quocchung.cntt1.techcycle_system.controller;

import com.quocchung.cntt1.techcycle_system.dtos.request.Brand.CreateBrandRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Brand.BrandResponse;
import com.quocchung.cntt1.techcycle_system.service.BrandService;
import com.quocchung.cntt1.techcycle_system.utils.PageResponseUtil;
import com.quocchung.cntt1.techcycle_system.utils.ResponseUtils;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {
  private final BrandService brandService;
  private final ResponseUtils responseUtils;

  @GetMapping("/getAllData")
  public ResponseEntity<APIResponse<BrandResponse>> getAllData(
      @RequestParam(name = "searchText", required = false) String searchText,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer size
  ) {
    PageRequest pageable = PageRequest.of(page - 1, size);
    Page<BrandResponse> brandPage = brandService.getAllData(searchText, pageable);
    PageResponse pageInfo = PageResponseUtil.extractPageMetadata(brandPage);
    return ResponseEntity.ok(responseUtils.successPage(brandPage.getContent(), pageInfo));
  }

  @GetMapping("/get-all-brand")
  public ResponseEntity<APIResponse<List<BrandResponse>>> getAllData(
      @RequestParam(name = "searchText", required = false) String searchText
  ){
    List<BrandResponse> allBrand = brandService.getAllData(searchText);
    return ResponseEntity.ok(responseUtils.success(allBrand));
  }

  @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<BrandResponse>> create(
      @Valid @ModelAttribute CreateBrandRequest request,
      @RequestParam("logo") MultipartFile logo
  ) {
    BrandResponse response = brandService.create(request, logo);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  @PutMapping(value = "/edit/{brandId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<APIResponse<BrandResponse>> update(
      @PathVariable Long brandId,
      @Valid @ModelAttribute CreateBrandRequest request,
      @RequestParam(value = "logo", required = false) MultipartFile logo
  ) {
    BrandResponse response = brandService.update(brandId, request, logo);
    return ResponseEntity.ok(responseUtils.success(response));
  }

  @DeleteMapping("/delete/{brandId}")
  public ResponseEntity<APIResponse<String>> delete(@PathVariable Long brandId) {
    brandService.delete(brandId);
    return ResponseEntity.ok(responseUtils.success("Delete brand successfully"));
  }

}

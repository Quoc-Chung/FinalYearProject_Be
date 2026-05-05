package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.dtos.request.Brand.CreateBrandRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Brand.BrandResponse;
import com.quocchung.cntt1.techcycle_system.dtos.response.Minio.StorageUploadResponse;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.model.Brand;
import com.quocchung.cntt1.techcycle_system.model.Category;
import com.quocchung.cntt1.techcycle_system.repository.BrandRepository;
import com.quocchung.cntt1.techcycle_system.repository.CategoryRepository;
import com.quocchung.cntt1.techcycle_system.service.BrandService;
import com.quocchung.cntt1.techcycle_system.service.MinIoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final MinIoService minIoService;

  @Override
  @Transactional
  public BrandResponse create(CreateBrandRequest request, MultipartFile logoFile) {
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Category not found"));

    Brand brand = Brand.builder()
        .category(category)
        .name(request.getName().trim())
        .isActive(request.getIsActive() == null || request.getIsActive())
        .build();
    brand = brandRepository.save(brand);

    if (logoFile != null && !logoFile.isEmpty()) {
      StorageUploadResponse uploadResponse = minIoService.uploadBrandLogo(logoFile, brand.getBrandId());
      brand.setLogoUrl(uploadResponse.getUrl());
      brand = brandRepository.save(brand);
    }
    return mapResponse(brand);
  }

  @Override
  @Transactional
  public BrandResponse update(Long brandId, CreateBrandRequest request, MultipartFile logoFile) {
    Brand brand = brandRepository.findById(brandId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Brand not found"));
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Category not found"));

    brand.setCategory(category);
    brand.setName(request.getName().trim());
    brand.setIsActive(request.getIsActive() == null ? brand.getIsActive() : request.getIsActive());

    if (logoFile != null && !logoFile.isEmpty()) {
      StorageUploadResponse uploadResponse = minIoService.uploadBrandLogo(logoFile, brand.getBrandId());
      brand.setLogoUrl(uploadResponse.getUrl());
    }
    brand = brandRepository.save(brand);
    return mapResponse(brand);
  }

  @Override
  @Transactional
  public void delete(Long brandId) {
    Brand brand = brandRepository.findById(brandId)
        .orElseThrow(() -> new ResException(ResErrorCode.ENTITY_NOT_EXISTS, "Brand not found"));
    brandRepository.delete(brand);
  }

  @Override
  public List<BrandResponse> getAllData(String searchText) {
    Sort sort = Sort.by(Sort.Direction.DESC, "brandId");
    List<Brand> brands = (searchText == null || searchText.isBlank())
        ? brandRepository.findAll(sort)
        : brandRepository.findByNameContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
            searchText.trim(), searchText.trim(), sort);
    return brands.stream().map(this::mapResponse).toList();
  }

  private BrandResponse mapResponse(Brand brand) {
    return BrandResponse.builder()
        .brandId(brand.getBrandId())
        .categoryId(brand.getCategory().getCategoryId())
        .categoryName(brand.getCategory().getName())
        .name(brand.getName())
        .logoUrl(brand.getLogoUrl())
        .isActive(brand.getIsActive())
        .build();
  }
}

package com.quocchung.cntt1.techcycle_system.service;

import com.quocchung.cntt1.techcycle_system.dtos.request.Brand.CreateBrandRequest;
import com.quocchung.cntt1.techcycle_system.dtos.response.Brand.BrandResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BrandService {
  BrandResponse create(CreateBrandRequest request, MultipartFile logoFile);

  BrandResponse update(Long brandId, CreateBrandRequest request, MultipartFile logoFile);

  void delete(Long brandId);

  List<BrandResponse> getAllData(String searchText);

  Page<BrandResponse> getAllData(String searchText, Pageable pageable);

}

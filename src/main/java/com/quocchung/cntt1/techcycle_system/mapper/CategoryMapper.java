package com.quocchung.cntt1.techcycle_system.mapper;

import com.quocchung.cntt1.techcycle_system.dtos.response.Category.CategoryTreeResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {
  List<CategoryTreeResponse> getAllCategories(@Param("searchText") String searchText);
}

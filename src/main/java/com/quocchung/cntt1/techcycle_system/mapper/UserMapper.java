package com.quocchung.cntt1.techcycle_system.mapper;

import com.quocchung.cntt1.techcycle_system.dtos.response.User.UserResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

  List<UserResponse> searchUsers(
      @Param("searchText") String searchText,
      @Param("status") String status,
      @Param("offset") int offset,
      @Param("limit") int limit
  );

  Long countSearchUsers(
      @Param("searchText") String searchText,
      @Param("status") String status
  );
}

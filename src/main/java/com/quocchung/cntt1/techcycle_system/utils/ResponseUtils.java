package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.ResponseStatus;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtils{

  public <T> APIResponse<T> success(T data) {
    APIResponse<T> response = new APIResponse<>();
    response.setStatus(new ResponseStatus(
        ResponseStatus.SUCCESS_CODE,
        ResponseStatus.SUCCESS_MESSAGE,
        ResponseStatus.SUCCESS_LABEL
    ));
    if (data instanceof List) {
      response.setData((List<T>) data);
    } else {
      response.setData(Collections.singletonList(data));
    }
    response.setPage(null);
    response.setExtraData(Collections.emptyMap());
    return response;
  }
  public <T> APIResponse<T> successList(List<T> data) {
    APIResponse<T> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(data);
    response.setPage(null);
    response.setExtraData(Collections.emptyMap());
    return response;
  }

  public <T> APIResponse<T> successPage(List<T> data, PageResponse pageInfo) {
    APIResponse<T> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(data);
    response.setPage(pageInfo);
    response.setExtraData(Collections.emptyMap());
    return response;
  }

  public <T> APIResponse<T> successPage(List<T> data, long currentPage, long totalElements, int size) {
    int totalPages = (int) Math.ceil((double) totalElements / size);
    if (totalPages == 0) totalPages = 1;

    PageResponse pageInfo = new PageResponse();
    pageInfo.setTotalPages(totalPages);
    pageInfo.setHasNext(currentPage < totalPages);
    pageInfo.setHasPrevious(currentPage > 1);
    pageInfo.setCurrentPage((int) currentPage);
    pageInfo.setTotalElements(totalElements);

    return successPage(data, pageInfo);
  }

}

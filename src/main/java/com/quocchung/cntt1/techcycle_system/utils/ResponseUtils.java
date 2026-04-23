package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
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
    response.setData(Collections.singletonList(data));
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

}

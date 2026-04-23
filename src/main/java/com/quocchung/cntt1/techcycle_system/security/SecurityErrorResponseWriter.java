package com.quocchung.cntt1.techcycle_system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.utils.MessageUtil;
import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {
  private final ObjectMapper objectMapper;

  public void write(HttpServletRequest request, HttpServletResponse response, ResErrorCode errorCode) throws IOException {
    APIResponse<Object> payload = new APIResponse<>();
    payload.setStatus(new ResponseStatus(errorCode.code(), resolveMessage(errorCode.message()), "Error"));
    payload.setData(Collections.emptyList());
    payload.setPage(null);

    Map<String, Object> extraData = new LinkedHashMap<>();
    extraData.put("timestamp", LocalDateTime.now());
    extraData.put("path", request.getRequestURI());
    payload.setExtraData(extraData);

    response.setStatus(errorCode.status().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getOutputStream(), payload);
  }

  private String resolveMessage(String messageKeyOrLiteral) {
    try {
      return MessageUtil.getMessage(messageKeyOrLiteral);
    } catch (Exception ex) {
      return messageKeyOrLiteral;
    }
  }
}

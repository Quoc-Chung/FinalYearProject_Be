package com.quocchung.cntt1.techcycle_system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.utils.MessageUtil;
import com.quocchung.cntt1.techcycle_system.utils.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {
  private final ObjectMapper objectMapper;

  public void write(HttpServletRequest request, HttpServletResponse response, ResErrorCode errorCode) throws IOException {
    ErrorResponse errorResponse = ErrorResponse.of(
        errorCode.code(),
        resolveMessage(errorCode.message()),
        request.getRequestURI()
    );

    response.setStatus(errorCode.status().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }

  private String resolveMessage(String messageKeyOrLiteral) {
    try {
      return MessageUtil.getMessage(messageKeyOrLiteral);
    } catch (Exception ex) {
      return messageKeyOrLiteral;
    }
  }
}

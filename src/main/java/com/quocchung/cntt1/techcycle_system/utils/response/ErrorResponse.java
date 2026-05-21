package com.quocchung.cntt1.techcycle_system.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String path,
    LocalDateTime timestamp,
    Map<String, String> errors
) {
  public static ErrorResponse of(String code, String message, String path) {
    return new ErrorResponse(code, message, path, LocalDateTime.now(), null);
  }

  public static ErrorResponse of(String code, String message, String path, Map<String, String> errors) {
    return new ErrorResponse(code, message, path, LocalDateTime.now(), errors);
  }
}
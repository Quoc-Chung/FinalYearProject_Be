package com.quocchung.cntt1.techcycle_system.handler;

import com.quocchung.cntt1.techcycle_system.exception.ResErrorCode;
import com.quocchung.cntt1.techcycle_system.exception.ResException;
import com.quocchung.cntt1.techcycle_system.utils.MessageUtil;
import com.quocchung.cntt1.techcycle_system.utils.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
  public ResponseEntity<ErrorResponse> handleIncorrectResultSize(
      IncorrectResultSizeDataAccessException ex, HttpServletRequest request) {
    log.error("IncorrectResultSizeDataAccessException: root cause={}", ex.getMostSpecificCause().getMessage(), ex);
    log.error("Full stack trace:", ex);
    Map<String, String> errors = new LinkedHashMap<>();
    errors.put("error", "Dữ liệu trùng lặp: " + ex.getMostSpecificCause().getMessage());
    return buildErrorResponse(ResErrorCode.GENERAL_ERROR, errors, request);
  }

  @ExceptionHandler(ResException.class)
  public ResponseEntity<ErrorResponse> handleResException(ResException ex, HttpServletRequest request) {
    HttpStatus status = ex.getStatus() != null ? ex.getStatus() : ResErrorCode.GENERAL_ERROR.status();
    String code = ex.getCode() != null ? ex.getCode() : ResErrorCode.GENERAL_ERROR.code();
    String message = resolveMessage(ex.getMessage(), ex.getMessageKey(), ex.getMessageParams());

    Map<String, String> errors = toStringMap(ex.getData());
    return ResponseEntity.status(status).body(ErrorResponse.of(code, message, request.getRequestURI(), errors));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

    return buildErrorResponse(ResErrorCode.BAD_REQUEST, errors, request);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
    return buildErrorResponse(ResErrorCode.BAD_REQUEST, errors, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getConstraintViolations().forEach(v -> {
      String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "unknown";
      errors.putIfAbsent(field, v.getMessage());
    });
    return buildErrorResponse(ResErrorCode.BAD_REQUEST, errors, request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    return buildErrorResponse(ResErrorCode.BAD_REQUEST,
        Map.of(ex.getParameterName(), "Parameter is required"), request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
    return buildErrorResponse(ResErrorCode.BAD_REQUEST, Map.of(ex.getName(), "Must be " + expected), request);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
    return buildErrorResponse(ResErrorCode.UNAUTHORIZED, null, request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    return buildErrorResponse(ResErrorCode.PERMISSION_DENIED, null, request);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
    return buildErrorResponse(ResErrorCode.ENTITY_NOT_EXISTS, null, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
    return buildErrorResponse(ResErrorCode.GENERAL_ERROR, null, request);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      ResErrorCode errorCode, Map<String, String> errors, HttpServletRequest request) {

    ErrorResponse body = ErrorResponse.of(
        errorCode.code(),
        resolveMessage(errorCode.message()),
        request.getRequestURI(),
        errors
    );
    return ResponseEntity.status(errorCode.status()).body(body);
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> toStringMap(Object data) {
    if (data instanceof Map<?, ?> raw) {
      Map<String, String> result = new LinkedHashMap<>();
      raw.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
      return result.isEmpty() ? null : result;
    }
    return null;
  }

  private String resolveMessage(String fallback, String messageKey, Object[] params) {
    String key = messageKey != null && !messageKey.isBlank() ? messageKey : fallback;
    if (key == null || key.isBlank()) {
      return ResErrorCode.GENERAL_ERROR.message();
    }
    try {
      if (params != null && params.length > 0) {
        return MessageUtil.getMessage(key, params);
      }
      return MessageUtil.getMessage(key);
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private String resolveMessage(String key) {
    return resolveMessage(key, null, null);
  }
}

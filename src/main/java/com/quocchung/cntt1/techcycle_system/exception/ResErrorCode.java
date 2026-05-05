package com.quocchung.cntt1.techcycle_system.exception;

import org.springframework.http.HttpStatus;

public enum ResErrorCode {

  SUCCESS(HttpStatus.OK, "200", "success"),

  PERMISSION_DENIED(HttpStatus.FORBIDDEN, "403", "permission.denied"),

  GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "general.error"),

  BAD_REQUEST(HttpStatus.BAD_REQUEST, "400", "bad.request"),

  ENTITY_NOT_EXISTS(HttpStatus.NOT_FOUND, "404", "entity.not.exists"),

  ENTITY_EXISTED(HttpStatus.CONFLICT, "409", "entity.existed"),

  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "429", "too.many.requests"),

  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "unauthorized"),

  // Auth errors
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "409", "auth.email.already.exists"),
  PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "400", "auth.password.mismatch"),
  DEFAULT_ROLE_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "500", "auth.default.role.missing"),
  USER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "403", "auth.user.not.active"),
  USER_BANNED(HttpStatus.FORBIDDEN, "403", "auth.user.banned"),
  USER_DELETED(HttpStatus.FORBIDDEN, "403", "auth.user.deleted"),
  RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "400", "auth.reset.token.invalid"),
  CURRENT_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "400", "auth.current.password.incorrect"),
  NEW_PASSWORD_MUST_DIFFER(HttpStatus.BAD_REQUEST, "400", "auth.new.password.must.differ"),
  UNSUPPORTED_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, "400", "auth.unsupported.social.type"),
  AUTH_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "400", "auth.code.required"),
  SOCIAL_TOKEN_EXCHANGE_FAILED(HttpStatus.UNAUTHORIZED, "401", "auth.social.token.exchange.failed"),
  SOCIAL_USER_INFO_FETCH_FAILED(HttpStatus.UNAUTHORIZED, "401", "auth.social.user.info.fetch.failed"),
  SOCIAL_ACCOUNT_NO_EMAIL(HttpStatus.BAD_REQUEST, "400", "auth.social.account.no.email"),
  EMPTY_RESPONSE(HttpStatus.UNAUTHORIZED, "401", "auth.empty.response"),
  FORGOT_PASSWORD_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "429", "auth.forgot.password.rate.limit"),
  EMAIL_NOT_EXISTS(HttpStatus.NOT_FOUND, "404", "auth.email.not.exists"),
  PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "400", "auth.password.incorrect"),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "400", "invalid.request");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ResErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public String code() { return code; }
  public HttpStatus status() { return status; }
  public String message() { return message; }
}

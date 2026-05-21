package com.quocchung.cntt1.techcycle_system.exception;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResException extends RuntimeException {

  private HttpStatus status;

  private String code;

  private String message;

  private Object data;

  private String messageKey;

  private Object[] messageParams;

  public ResException() {
    super();
  }

  public ResException(ResErrorCode code) {
    super();
    this.code = code.code();
    this.message = code.message();
    this.status = code.status();
    this.messageKey = code.message();
  }

  public ResException(ResErrorCode code, Map<?, ?> data) {
    super();
    this.code = code.code();
    this.message = code.message();
    this.status = code.status();
    this.data = data;
    this.messageKey = code.message();
  }

  public ResException(ResErrorCode code, HttpStatus status, Map<?, ?> data) {
    super();
    this.code = code.code();
    this.message = code.message();
    this.status = status;
    this.data = data;
    this.messageKey = code.message();
  }

  public ResException(ResErrorCode code, String message) {
    super();
    this.code = code.code();
    this.message = message;
    this.status = code.status();
  }

  public ResException(ResErrorCode code, String messageKey, Object[] params) {
    super();
    this.code = code.code();
    this.status = code.status();
    this.messageKey = messageKey;
    this.messageParams = params;
  }

  public ResException(ResErrorCode code, Object[] params) {
    super();
    this.code = code.code();
    this.status = code.status();
    this.messageKey = code.message();
    this.messageParams = params;
  }

  public ResException(ResErrorCode code, String field, String message) {
    super();
    this.code = code.code();
    this.message = code.message();
    this.status = code.status();
    Map<String, String> map = new HashMap<>();
    map.put(field, message);
    this.data = map;
  }

  public ResException(ResErrorCode code, String... errors) {
    super();
    this.code = code.code();
    this.message = code.message();
    this.status = code.status();
    this.messageKey = code.message();

    if (errors != null) {
      Map<String, String> map = new HashMap<String, String>();
      for (int i = 0; i < errors.length / 2; i++) {
        map.put(errors[i * 2], errors[i * 2 + 1]);
      }
      this.data = map;
    }
  }

}

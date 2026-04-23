package com.quocchung.cntt1.techcycle_system.utils.response;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class ResponseStatus {

  public static final String SUCCESS_LABEL = "Success";
  public static final String SUCCESS_CODE = "200";
  public static final String SUCCESS_MESSAGE = "Thành công";
  public static final ResponseStatus SUCCESS_STATUS = new ResponseStatus(SUCCESS_CODE, SUCCESS_MESSAGE, SUCCESS_LABEL);

  private String code;
  private String message;
  private String label;

  public ResponseStatus() {

  }
  public ResponseStatus(String code, String message, String label) {
    this.code = code;
    this.message = message;
    this.label = label;
  }
}
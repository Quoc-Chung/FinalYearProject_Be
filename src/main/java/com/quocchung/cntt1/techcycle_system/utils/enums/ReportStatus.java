package com.quocchung.cntt1.techcycle_system.utils.enums;

public enum ReportStatus {
  PENDING,    // Chờ admin xử lý
  APPROVED,   // Admin xác nhận vi phạm → post = VIOLATION
  REJECTED    // Admin bác bỏ, không vi phạm → post = APPROVED lại
}
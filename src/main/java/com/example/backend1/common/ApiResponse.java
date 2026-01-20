package com.example.backend1.common;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
    boolean success,
    String code,
    String message,
    T data,
    OffsetDateTime timestamp
) {
  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "OK", "success", data, OffsetDateTime.now());
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, "OK", message, data, OffsetDateTime.now());
  }

  public static ApiResponse<Void> error(String code, String message) {
    return new ApiResponse<>(false, code, message, null, OffsetDateTime.now());
  }
}

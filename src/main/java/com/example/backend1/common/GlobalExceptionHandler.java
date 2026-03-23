package com.example.backend1.common;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApi(ApiException e) {
    var ec = e.errorCode();
    return ResponseEntity.status(ec.status()).body(ApiResponse.error(ec.code(), e.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
  public ResponseEntity<ApiResponse<Void>> handleValidation(Exception e) {
    return ResponseEntity.status(ErrorCode.INVALID_INPUT.status())
        .body(ApiResponse.error(ErrorCode.INVALID_INPUT.code(), ErrorCode.INVALID_INPUT.message()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException e) {
    return ResponseEntity.status(ErrorCode.AUTH_FAILED.status())
        .body(ApiResponse.error(ErrorCode.AUTH_FAILED.code(), ErrorCode.AUTH_FAILED.message()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException e) {
    return ResponseEntity.status(ErrorCode.ACCESS_DENIED.status())
        .body(ApiResponse.error(ErrorCode.ACCESS_DENIED.code(), ErrorCode.ACCESS_DENIED.message()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
    log.error("Unhandled error", e);
    return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
        .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.message()));
  }
}

package com.example.backend1.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),
  AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "인증에 실패했습니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "권한이 없습니다."),
  AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "AI 분석 서버가 응답하지 않습니다."),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
  USERNAME_DUPLICATE(HttpStatus.CONFLICT, "USERNAME_DUPLICATE", "이미 사용 중인 아이디입니다."),
  EMAIL_DUPLICATE(HttpStatus.CONFLICT, "EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다."),
  PHONE_DUPLICATE(HttpStatus.CONFLICT, "PHONE_DUPLICATE", "이미 사용 중인 휴대폰번호입니다."),
  EMAIL_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_REQUIRED", "이메일 인증이 필요합니다."),
  EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_EXPIRED", "이메일 인증 코드가 만료되었습니다."),
  EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_INVALID", "이메일 인증 코드가 올바르지 않습니다."),
  EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", "이메일 전송에 실패했습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "HISTORY_NOT_FOUND", "이력을 찾을 수 없습니다."),
  DIAGNOSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSIS_NOT_FOUND", "진단을 찾을 수 없습니다."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public HttpStatus status() { return status; }
  public String code() { return code; }
  public String message() { return message; }
}

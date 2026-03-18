package com.example.backend1.user.domain;

import jakarta.persistence.*;

@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;
  private String password;
  private String email;
  private String phoneNumber;
  private String residenceType;
  private Boolean isRenter;

  // 기본 생성자
  public User() {}

  // 생성자
  public User(
          String username,
          String password,
          String email,
          String phoneNumber,
          String residenceType,
          Boolean isRenter
  ) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.residenceType = residenceType;
    this.isRenter = isRenter;
  }

  // 🔥 Getter (핵심)
  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getResidenceType() {
    return residenceType;
  }

  public Boolean getIsRenter() {
    return isRenter;
  }

  // Setter (필요한 것만)
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setResidenceType(String residenceType) {
    this.residenceType = residenceType;
  }
}
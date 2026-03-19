package com.example.backend1.user.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_phone_number", columnList = "phoneNumber", unique = true)
})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, length = 30)
  private String phoneNumber;

  // ✅ 추가
  @Column(unique = true, length = 255)
  private String email;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ResidenceType residenceType = ResidenceType.ETC;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RentType rentType = RentType.NONE;

  @Column(nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  protected User() {}

<<<<<<< HEAD
  // ✅ 생성자 수정
  public User(String username, String passwordHash, String phoneNumber, String email) {
=======
  public User(String username, String email, String passwordHash, String phoneNumber) {
>>>>>>> 54853b61a9ad007f59f1bc6b2ecbd171b008dcd6
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.phoneNumber = phoneNumber;
    this.email = email;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public String getPhoneNumber() { return phoneNumber; }
  public String getEmail() { return email; } // ✅ 추가
  public String getAddress() { return address; }
  public ResidenceType getResidenceType() { return residenceType; }
  public RentType getRentType() { return rentType; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void updateProfile(
          ResidenceType residenceType,
          RentType rentType,
          String phoneNumber,
          String address
  ) {
    if (residenceType != null) this.residenceType = residenceType;
    if (rentType != null) this.rentType = rentType;
    if (phoneNumber != null) this.phoneNumber = phoneNumber;
    if (address != null) this.address = address;
  }
}
package com.example.backend1.user.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, length = 30)
  private String phoneNumber;

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

  public User(String username, String passwordHash, String phoneNumber) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.phoneNumber = phoneNumber;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
  public String getPhoneNumber() { return phoneNumber; }
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

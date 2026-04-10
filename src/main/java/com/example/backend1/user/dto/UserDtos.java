package com.example.backend1.user.dto;

import com.example.backend1.user.domain.RentType;
import com.example.backend1.user.domain.ResidenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UserDtos {

  public record MeResponse(
          Long id,
          String username,
          String phoneNumber,
          String address,
          ResidenceType residenceType,
          RentType rentType
  ) {}

  public record UpdateProfileRequest(
          @NotNull ResidenceType residenceType,
          @NotNull RentType rentType,
          @Pattern(
                  regexp = "^[0-9\\-+() ]{8,20}$",
                  message = "전화번호 형식이 올바르지 않습니다."
          )
          String phoneNumber,
          String address
  ) {}
}

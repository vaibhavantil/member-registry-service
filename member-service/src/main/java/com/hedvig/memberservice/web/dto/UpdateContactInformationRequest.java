package com.hedvig.memberservice.web.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Data
public class UpdateContactInformationRequest {

  private String memberId;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Address address;
  @Nullable
  private LocalDate birthDate;
}

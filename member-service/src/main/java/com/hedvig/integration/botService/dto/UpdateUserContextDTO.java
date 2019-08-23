package com.hedvig.integration.botService.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UpdateUserContextDTO {

  private String memberId;
  private String personalNumber;

  private String firstName;
  private String lastName;

  private String phoneNumber;
  private String email;

  private String street;
  private String city;
  private String zipCode;

  private boolean hasSigned;
}

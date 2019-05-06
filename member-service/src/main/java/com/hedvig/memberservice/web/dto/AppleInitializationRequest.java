package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class AppleInitializationRequest {
  private String memberId;
  private String personalNumber;

  private String firstName;
  private String lastName;

  private String phoneNumber;
  private String email;

  private String street;
  private String city;
  private String zipCode;
}

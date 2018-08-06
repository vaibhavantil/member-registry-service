package com.hedvig.memberservice.aggregates;

import lombok.Data;

@Data
public class Member {
  private String firstName;
  private String lastName;
  private String ssn;
  private String email;
  private String phoneNumber;

  private LivingAddress livingAddress;
}

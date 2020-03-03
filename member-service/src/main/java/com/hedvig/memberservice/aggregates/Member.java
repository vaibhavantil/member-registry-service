package com.hedvig.memberservice.aggregates;

import com.hedvig.memberservice.web.dto.Market;
import lombok.Data;

@Data
public class Member {
  private String firstName;
  private String lastName;
  private String ssn;
  private String email;
  private String phoneNumber;

  private String acceptLanguage;

  private Market market;

  private LivingAddress livingAddress;
}

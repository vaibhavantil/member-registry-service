package com.hedvig.memberservice.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Profile {

  private String name;
  private String firstName;
  private String lastName;
  private List<String> familyMembers;
  private Integer age;
  private String email;
  private String address;
  private Integer livingAreaSqm;
  private String maskedBankAccountNumber;
  private String selectedCashback;

  private String paymentStatus;
  private LocalDate nextPaymentDate;

  private String selectedCashbackSignature;
  private String selectedCashbackParagraph;
  private String selectedCashbackImageUrl;

  private List<String> safetyIncreasers;
  private UUID trackingId;
}

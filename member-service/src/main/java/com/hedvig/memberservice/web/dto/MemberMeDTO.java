package com.hedvig.memberservice.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MemberMeDTO {
    String memberId;
    String ssn;
    String name;
    String firstName;
    String lastName;
    LocalDate birthDate;
    List<String> familyMembers;
    Integer age;
    String email;
    String address;
    Integer livingAreaSqm;
    String maskedBankAccountNumber;
    String selectedCashback;

    String paymentStatus;
    LocalDate nextPaymentDate;

    String selectedCashbackSignature;
    String selectedCashbackParagraph;
    String selectedCashbackImageUrl;

    List<String> safetyIncreasers;
    UUID trackingId;
    String phoneNumber;
}

package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.query.MemberEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hedvig.memberservice.util.Gender;
import com.hedvig.memberservice.util.SsnUtilImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;

@Data
@NoArgsConstructor
public class InternalMember {
  private Long memberId;

  private String status;

  private String ssn;

  private Gender gender;

  private String firstName;

  private String lastName;

  private String street;

  private Integer floor;

  private String apartment;

  private String city;

  private String zipCode;

  private String country;

  private String email;

  private String phoneNumber;

  private LocalDate birthDate;

  private Instant signedOn;

  private Instant createdOn;

  private String fraudulentStatus;

  private String fraudulentDescription;

  private String acceptLanguage;

  private List<TraceMemberDTO> traceMemberInfo = new ArrayList<>();

  public static InternalMember fromEntity(MemberEntity entity) {
    val dto = new InternalMember();
    dto.setMemberId(entity.getId());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "");
    dto.setSsn(entity.getSsn());
    dto.setGender(SsnUtilImpl.getInstance().getGenderFromSsn(entity.getSsn()));
    dto.setFirstName(entity.getFirstName());
    dto.setLastName(entity.getLastName());
    dto.setStreet(entity.getStreet());
    dto.setFloor(entity.getFloor());
    dto.setApartment(entity.getApartment());
    dto.setCity(entity.getCity());
    dto.setZipCode(entity.getZipCode());
    dto.setCountry("SE");
    dto.setEmail(entity.getEmail());
    dto.setPhoneNumber(entity.getPhoneNumber());
    dto.setBirthDate(entity.getBirthDate());
    dto.setSignedOn(entity.getSignedOn());
    dto.setCreatedOn(entity.getCreatedOn());
    dto.setFraudulentStatus(entity.getFraudulentStatus() != null ? entity.getFraudulentStatus().name() : "");
    dto.setFraudulentDescription(entity.getFraudulentDescription());
    dto.setAcceptLanguage(entity.getAcceptLanguage());
    return dto;
  }
}

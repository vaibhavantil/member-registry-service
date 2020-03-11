package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.time.LocalDate;

@Value
public class MemberUpdateContactInformationCommand {

  @TargetAggregateIdentifier
  long memberId;

  String firstName;
  String lastName;
  String email;
  String phoneNumber;
  String street;
  String city;
  String zipCode;
  String apartmentNo;
  Integer floor;
  LocalDate birthDate;

  public MemberUpdateContactInformationCommand(
      Long memberId, UpdateContactInformationRequest body) {
    this.memberId = memberId;

    this.firstName = body.getFirstName();
    this.lastName = body.getLastName();
    this.email = body.getEmail();

    if (body.getAddress() != null) {
      this.street = body.getAddress().getStreet();
      this.city = body.getAddress().getCity();
      this.zipCode = body.getAddress().getZipCode();
      this.apartmentNo = body.getAddress().getApartmentNo();
      this.floor = body.getAddress().getFloor();
    } else {
      street = null;
      city = null;
      zipCode = null;
      apartmentNo = null;
      floor = null;
    }

    this.phoneNumber = body.getPhoneNumber();
    this.birthDate = body.getBirthDate();
  }
}

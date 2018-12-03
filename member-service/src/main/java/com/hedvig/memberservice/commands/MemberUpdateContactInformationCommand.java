package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

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
  String token;

  public MemberUpdateContactInformationCommand(
      Long memberId, UpdateContactInformationRequest body, String token) {
    this.memberId = memberId;

    this.firstName = body.getFirstName();
    this.lastName = body.getLastName();
    this.email = body.getEmail();

    this.street = body.getAddress().getStreet();
    this.city = body.getAddress().getCity();
    this.zipCode = body.getAddress().getZipCode();
    this.apartmentNo = body.getAddress().getApartmentNo();
    this.floor = body.getAddress().getFloor();
    this.phoneNumber = body.getPhoneNumber();
    this.token = token;
  }
}

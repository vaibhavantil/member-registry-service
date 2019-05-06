package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.AppleInitializationRequest;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class InitializeAppleUserCommand {

  @TargetAggregateIdentifier
  private long memberId;

  private String personalNumber;
  private String firstName;
  private String lastName;

  private String phoneNumber;
  private String email;

  private String street;
  private String city;
  private String zipCode;


  public static InitializeAppleUserCommand from(AppleInitializationRequest req){
    return new InitializeAppleUserCommand(
      Long.parseLong(req.getMemberId()),
      req.getPersonalNumber(),
      req.getFirstName(),
      req.getLastName(),
      req.getPhoneNumber(),
      req.getEmail(),
      req.getStreet(),
      req.getCity(),
      req.getZipCode()
    );
  }
}

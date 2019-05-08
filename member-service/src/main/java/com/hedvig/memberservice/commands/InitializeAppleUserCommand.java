package com.hedvig.memberservice.commands;

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
}

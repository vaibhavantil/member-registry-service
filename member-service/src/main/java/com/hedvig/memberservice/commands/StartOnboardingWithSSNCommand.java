package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

//This is deprecated and will only work for sweden should removet once bot-service is not used for on boarding
@Value
@Deprecated
public class StartOnboardingWithSSNCommand {
  @TargetAggregateIdentifier long memberId;
  String ssn;

  public StartOnboardingWithSSNCommand(long memberId, StartOnboardingWithSSNRequest requestData) {
    this.memberId = memberId;
    this.ssn = requestData.getSsn();
  }
}

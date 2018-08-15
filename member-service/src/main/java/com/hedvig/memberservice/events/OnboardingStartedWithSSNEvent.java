package com.hedvig.memberservice.events;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OnboardingStartedWithSSNEvent extends SSNUpdatedEvent {

  public OnboardingStartedWithSSNEvent(Long memberId, String ssn) {
    super(memberId, ssn);
  }

}

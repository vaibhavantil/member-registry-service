package com.hedvig.memberservice.events;

import lombok.EqualsAndHashCode;
import org.axonframework.serialization.Revision;

//This event is deprecated don't use it for any new stuff
@EqualsAndHashCode(callSuper = true)
@Deprecated
@Revision("1.0")
public class OnboardingStartedWithSSNEvent extends SSNUpdatedEvent {

  public OnboardingStartedWithSSNEvent(Long memberId, String ssn, Nationality nationality) {
    super(memberId, ssn, nationality);
  }

    @Override
    public String getSsn() {
        return super.getSsn();
    }
}

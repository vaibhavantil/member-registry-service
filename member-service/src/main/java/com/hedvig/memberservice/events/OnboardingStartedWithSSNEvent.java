package com.hedvig.memberservice.events;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class OnboardingStartedWithSSNEvent extends SSNUpdatedEvent {

    public OnboardingStartedWithSSNEvent(Long memberId, String ssn) {
        super(memberId, ssn);
    }
}

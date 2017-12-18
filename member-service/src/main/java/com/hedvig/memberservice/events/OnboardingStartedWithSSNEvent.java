package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class OnboardingStartedWithSSNEvent extends SSNUpdatedEvent {

    public OnboardingStartedWithSSNEvent(Long memberId, String ssn) {
        super(memberId, ssn);
    }
}

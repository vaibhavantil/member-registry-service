package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class StartOnboardingWithSSNCommand {
    @TargetAggregateIdentifier
    long memberId;
    String ssn;

    public StartOnboardingWithSSNCommand(long memberId, StartOnboardingWithSSNRequest requestData) {
        this.memberId = memberId;
        this.ssn = requestData.getSsn();
    }
}

package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class SelectNewCashbackCommand {
    @TargetAggregateIdentifier
    private final Long memberId;
    private final UUID optionId;

    public SelectNewCashbackCommand(Long memberId, UUID optionId) {

        this.memberId = memberId;
        this.optionId = optionId;
    }
}

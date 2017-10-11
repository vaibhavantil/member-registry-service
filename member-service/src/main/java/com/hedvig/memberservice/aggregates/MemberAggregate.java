package com.hedvig.memberservice.aggregates;

import com.hedvig.memberservice.commands.CreateMemberCommand;
import com.hedvig.memberservice.commands.StartOnBoardingCommand;
import com.hedvig.memberservice.events.MemberStartedOnBoarding;
import com.hedvig.memberservice.events.MemberCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * This is an example Aggregate and should be remodeled to suit the needs of you domain.
 */
@Aggregate
public class MemberAggregate {

    @AggregateIdentifier
    public Long id;

    private MemberStatus status;

    private PersonInformation personInformation;

    public MemberAggregate(){

    }

    @CommandHandler
    public MemberAggregate(CreateMemberCommand command) {
        apply(new MemberCreatedEvent(command.getMemberId(), MemberStatus.INITIATED));
    }

    @CommandHandler void startOnBoarding(StartOnBoardingCommand command) throws Exception {
        if(this.status != MemberStatus.INITIATED)
            throw new RuntimeException("Member is not a prospect.");

        PersonInformation pi = new PersonInformation(command.getPerson());
        pi.setSsn(command.getBankIdStatus().getSSN());

        apply(new MemberStartedOnBoarding(
                command.getId(),
                MemberStatus.ONBOARDING,
                pi));
    }

    @EventSourcingHandler
    public void on(MemberCreatedEvent e) {
        this.id = e.getId();
        this.status = e.getStatus();
    }

    @EventSourcingHandler
    public void on(MemberStartedOnBoarding e){
        this.personInformation = e.getPersonInformation();
        this.status = e.getNewStatus();
    }
}


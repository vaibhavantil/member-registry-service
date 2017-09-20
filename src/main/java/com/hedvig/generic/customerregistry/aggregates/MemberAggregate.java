package com.hedvig.generic.customerregistry.aggregates;

import com.hedvig.generic.customerregistry.commands.ConvertAfterBankIdAuthCommand;
import com.hedvig.generic.customerregistry.commands.CreateMemberCommand;
import com.hedvig.generic.customerregistry.events.MemberConvertedAfterBankIdAuth;
import com.hedvig.generic.customerregistry.events.MemberCreatedEvent;
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

    // Retrieved from BankId
    public String name;
    public String givenName;
    public String surName;

    public String personalIdentificationNumber;

    public MemberStatus status;

    public MemberAggregate(){

    }

    @CommandHandler
    public MemberAggregate(CreateMemberCommand command) {
        apply(new MemberCreatedEvent(command.getMemberId(), MemberStatus.PROSPECT));
    }

    @CommandHandler void ConverFromProspect(ConvertAfterBankIdAuthCommand command) throws Exception {
        if(this.status != MemberStatus.PROSPECT)
            throw new Exception("Member is not a prospect.");

        apply(new MemberConvertedAfterBankIdAuth(
                command.getId(),
                command.getPersonalIdentificationNumber(),
                command.getGivenName(),
                command.getSurname(),
                command.getName(),
                MemberStatus.MEMBER));
    }

    @EventSourcingHandler
    public void on(MemberCreatedEvent e) {
        this.id = e.getId();
        this.status = e.getStatus();
    }

    @EventSourcingHandler
    public void on(MemberConvertedAfterBankIdAuth e){
        this.personalIdentificationNumber = e.getPersonalIdentificationNumber();
        this.status = e.getNewStatus();
        this.name = e.getName();
        this.givenName = e.getGivenName();
        this.surName = e.getSurname();
    }
}


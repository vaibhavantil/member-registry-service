package com.hedvig.memberservice.aggregates;

import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.external.bisnodeBCI.dto.Person;
import com.hedvig.external.bisnodeBCI.dto.PersonSearchResult;
import com.hedvig.memberservice.aggregates.exceptions.BankIdReferenceUsedException;
import com.hedvig.memberservice.commands.AuthenticationAttemptCommand;
import com.hedvig.memberservice.commands.BankIdSignCommand;
import com.hedvig.memberservice.commands.CreateMemberCommand;
import com.hedvig.memberservice.commands.InactivateMemberCommand;
import com.hedvig.memberservice.events.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.ApplyMore;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * This is an example Aggregate and should be remodeled to suit the needs of you domain.
 */
@Aggregate
public class MemberAggregate {

    @AggregateIdentifier
    public Long id;

    private Logger log = LoggerFactory.getLogger(MemberAggregate.class);

    private  BisnodeClient bisnodeClient;

    private MemberStatus status;

    private PersonInformation personInformation;

    private Set<String> authedReferenceTokens = new HashSet<>();

    @Autowired
    public MemberAggregate(BisnodeClient bisnodeClient) {
        this.bisnodeClient = bisnodeClient;
    }

    @CommandHandler
    public MemberAggregate(CreateMemberCommand command) {
        apply(new MemberCreatedEvent(command.getMemberId(), MemberStatus.INITIATED));
    }

    @CommandHandler
    void authAttempt(AuthenticationAttemptCommand command) {

        /*
        if(authedReferenceTokens.contains(command.getBankIdAuthResponse().getReferenceToken())) {
            throw new BankIdReferenceUsedException("BankId reference token already used.");
        }*/

        ApplyMore applyChain = null;

        if(this.status == MemberStatus.INITIATED) {
            //Trigger fetching of bisnode data.
            String ssn = command.getBankIdAuthResponse().getSSN();
            List<PersonSearchResult> personList = bisnodeClient.match(ssn).getPersons();
            if (personList.size() != 1) {
                log.error("Could not find person based on personnumer!");
                throw new RuntimeException("Could not find person at bisnode.");
            }

            Person person = personList.get(0).getPerson();

            PersonInformation pi = new PersonInformation(ssn, person);


            applyChain = apply(new MemberStartedOnBoardingEvent(
                    this.id,
                    MemberStatus.ONBOARDING,
                    pi));
        }

        MemberAuthenticatedEvent authenticatedEvent = new MemberAuthenticatedEvent(this.id, command.getBankIdAuthResponse().getReferenceToken());

        if(applyChain != null)
        {
            applyChain.andThenApply( () ->  authenticatedEvent);
        }
        else {
            apply(authenticatedEvent);
        }

    }

    @CommandHandler
    void inactivateMember(InactivateMemberCommand command) {
        if(this.status == MemberStatus.INITIATED) {
            apply(new MemberInactivatedEvent(this.id));
        } else {
            String str = String.format("Cannot INACTIAVTE member %s in status: %s", this.id, this.status.name());
            throw new RuntimeException(str);
        }
    }

    @CommandHandler
    void bankIdSignHandler(BankIdSignCommand cmd) {
        apply(new MemberSignedEvent(this.id, cmd.getReferenceId()));
    }

    @EventSourcingHandler
    public void on(MemberCreatedEvent e) {
        this.id = e.getId();
        this.status = e.getStatus();
    }

    @EventSourcingHandler
    public void on(MemberStartedOnBoardingEvent e){
        this.personInformation = e.getPersonInformation();
        this.status = e.getNewStatus();
    }

    @EventSourcingHandler
    public void on(MemberAuthenticatedEvent e) {
        this.authedReferenceTokens.add(e.getBankIdReferenceToken());
    }
}


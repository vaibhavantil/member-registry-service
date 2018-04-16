package com.hedvig.memberservice.aggregates;

import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.external.bisnodeBCI.dto.Person;
import com.hedvig.external.bisnodeBCI.dto.PersonSearchResult;
import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.events.*;
import com.hedvig.memberservice.services.CashbackService;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.ApplyMore;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

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

    private Member member;

    private BisnodeInformation latestBisnodeInformation;

    private CashbackService cashbackService;

    @Autowired
    public MemberAggregate(BisnodeClient bisnodeClient, CashbackService cashbackService) {
        this.bisnodeClient = bisnodeClient;
        this.cashbackService = cashbackService;
    }

    @CommandHandler
    public MemberAggregate(CreateMemberCommand command) {
        apply(new MemberCreatedEvent(command.getMemberId(), MemberStatus.INITIATED));
    }

    @CommandHandler
    void authAttempt(AuthenticationAttemptCommand command) {

        ApplyMore applyChain = null;

        BankIdAuthenticationStatus bankIdAuthResponse = command.getBankIdAuthResponse();
        if(this.status == MemberStatus.INITIATED || this.status == MemberStatus.ONBOARDING) {
            //Trigger fetching of bisnode data.
            String ssn = bankIdAuthResponse.getSSN();
            applyChain = apply(new SSNUpdatedEvent(this.id, ssn));

            try {
                applyChain = getPersonInformationFromBisnode(applyChain, ssn);
            }catch(RuntimeException ex) {
                log.error("Caught exception calling bisnode for personalInformation", ex);
                applyChain = applyChain.andThenApply(() ->
                        new NameUpdatedEvent(this.id, formatName(bankIdAuthResponse.getGivenName()), formatName(bankIdAuthResponse.getSurname())));
            }

            if(this.status == MemberStatus.INITIATED ){
	            applyChain = applyChain.
	                    andThenApply(() -> new MemberStartedOnBoardingEvent(this.id, MemberStatus.ONBOARDING));
            }
        }

        MemberAuthenticatedEvent authenticatedEvent = new MemberAuthenticatedEvent(this.id, bankIdAuthResponse.getReferenceToken());

        if(applyChain != null)
        {
            applyChain.andThenApply( () ->  authenticatedEvent);
        }
        else {
            apply(authenticatedEvent);
        }
    }

    private String formatName(String name) {
        String lowercase = name.toLowerCase();
        return  Character.toUpperCase(lowercase.charAt(0)) + lowercase.substring(1);
    }

    private ApplyMore getPersonInformationFromBisnode(ApplyMore applyChain, String ssn) throws RuntimeException {
        log.info("Calling bisnode for person information for {}", ssn);
        List<PersonSearchResult> personList = bisnodeClient.match(ssn).getPersons();
        Person person = personList.get(0).getPerson();
        if (personList.size() != 1) {
            throw new RuntimeException("Could not find person at bisnode.");
        }


        applyChain = applyChain.andThenApply(() -> new NameUpdatedEvent(this.id, getFirstName(person), person.getFamilyName()));

        BisnodeInformation pi = new BisnodeInformation(ssn, person);
        if(pi.getAddress().isPresent()) {
            applyChain = applyChain.andThenApply(() -> new LivingAddressUpdatedEvent(this.id, pi.getAddress().get()));
        }
        applyChain.andThenApply(() -> new PersonInformationFromBisnodeEvent(this.id, pi));
        return applyChain;
    }

    private String getFirstName(Person person) throws RuntimeException {
        if(person.getPreferredFirstName() == null) {
            if(person.getFirstNames() == null || person.getFirstNames().size() == 0) {
                throw new RuntimeException("Could not find firstname in bisnode response, prefferedFirstName and firstNames are null");
            }
            return person.getFirstNames().get(0);
        }

        return person.getPreferredFirstName();
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
    void startOnboardingWithSSNCommand(StartOnboardingWithSSNCommand comand) {
        apply(new OnboardingStartedWithSSNEvent(this.id, comand.getSsn()));
        apply(new MemberStartedOnBoardingEvent(this.id, MemberStatus.ONBOARDING));
    }

    @CommandHandler
    void memberUpdateContactInformation(MemberUpdateContactInformationCommand cmd) {

        if(!Objects.equals(this.member.getFirstName(),cmd.getFirstName()) ||
                !Objects.equals(this.member.getLastName(), cmd.getLastName())) {
            apply(new NameUpdatedEvent(this.id, cmd.getFirstName(), cmd.getLastName()));
        }

        if(Objects.equals(member.getEmail(), cmd.getEmail()) == false) {
            apply(new EmailUpdatedEvent(this.id, cmd.getEmail()));
        }


        LivingAddress address = this.member.getLivingAddress();
        if(address == null || address.needsUpdate(cmd.getStreet(), cmd.getCity(), cmd.getZipCode(), cmd.getApartmentNo(), cmd.getFloor())) {
            apply(new LivingAddressUpdatedEvent(this.id, cmd.getStreet(), cmd.getCity(), cmd.getZipCode(), cmd.getApartmentNo(), cmd.getFloor()));
        }
    }

    @CommandHandler
    void bankIdSignHandler(BankIdSignCommand cmd) {
        apply(new NewCashbackSelectedEvent(this.id, cashbackService.getDefaultId().toString()));
        apply(new MemberSignedEvent(this.id, cmd.getReferenceId(), cmd.getSignature(), cmd.getOscpResponse()));
    }

    @CommandHandler
    void selectNewCashback(SelectNewCashbackCommand cmd) {
        apply(new NewCashbackSelectedEvent(this.id, cmd.getOptionId().toString()));
    }

    /*
    @CommandHandler
    void finalizeOnBoarding(MemberUpdateContactInformationCommand cmd) {
        apply(new EmailUpdatedEvent(this.id, cmd.getEmail()));
        apply(new LivingAddressUpdatedEvent(this.id, cmd.getStreet(), cmd.getCity(), cmd.getZipCode(), cmd.getApartmentNo()));
        apply(new NameUpdatedEvent(this.id, cmd.getFirstName(), cmd.getLastName()));
        apply(new SSNUpdatedEvent(this.id, cmd.getSsn()));
    }*/

    @EventSourcingHandler
    public void on(MemberCreatedEvent e) {
        this.id = e.getId();
        this.status = e.getStatus();
        this.member = new Member();
    }

    @EventSourcingHandler
    public void on(MemberStartedOnBoardingEvent e){
        this.status = e.getNewStatus();
    }

    @EventSourcingHandler
    public void on(PersonInformationFromBisnodeEvent e) {
        this.latestBisnodeInformation = e.getInformation();
    }

    @EventSourcingHandler
    public void on(EmailUpdatedEvent e) {
        this.member.setEmail(e.getEmail());
    }

    @EventSourcingHandler
    public void on(LivingAddressUpdatedEvent e) {

        LivingAddress address = new LivingAddress(e.getStreet(), e.getCity(), e.getZipCode(), e.getApartmentNo(), e.getFloor());
        this.member.setLivingAddress(address);
    }

    @EventSourcingHandler
    public  void on(OnboardingStartedWithSSNEvent e) {
        this.member.setSsn(e.getSsn());
    }

}


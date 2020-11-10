package com.hedvig.memberservice.aggregates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.common.UUIDGenerator;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.external.bisnodeBCI.dto.Person;
import com.hedvig.external.bisnodeBCI.dto.PersonSearchResult;
import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket;
import com.hedvig.memberservice.events.*;
import com.hedvig.memberservice.services.cashback.CashbackService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.ApplyMore;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * This is an example Aggregate and should be remodeled to suit the needs of you domain.
 */
@Slf4j
@Aggregate
public class MemberAggregate {

  @AggregateIdentifier
  public Long id;

  private BisnodeClient bisnodeClient;

  private MemberStatus status;

  private Member member;

  private BisnodeInformation latestBisnodeInformation;

  private CashbackService cashbackService;

  private ObjectMapper objectMapper;

  private UUIDGenerator uuidGenerator;
  private UUID trackingId;

  @Autowired
  public MemberAggregate(
    BisnodeClient bisnodeClient,
    CashbackService cashbackService,
    UUIDGenerator uuidGenerator,
    ObjectMapper objectMapper) {
    this.bisnodeClient = bisnodeClient;
    this.cashbackService = cashbackService;
    this.uuidGenerator = uuidGenerator;
    this.objectMapper = objectMapper;
  }

  @CommandHandler
  public MemberAggregate(CreateMemberCommand command) {
    apply(new MemberCreatedEvent(command.getMemberId(), MemberStatus.INITIATED))
      .andThenApply(() -> {
        if (command.getAcceptLanguage() != null && !command.getAcceptLanguage().isEmpty()) {
          return new AcceptLanguageUpdatedEvent(command.getMemberId(), command.getAcceptLanguage());
        }
        return null;
      });
  }

  @CommandHandler
  void authAttempt(AuthenticationAttemptCommand command) {

    ApplyMore applyChain = null;

    BankIdAuthenticationStatus bankIdAuthResponse = command.getBankIdAuthResponse();
    if (this.status == MemberStatus.INITIATED || this.status == MemberStatus.ONBOARDING) {
      // Trigger fetching of bisnode data.
      String ssn = bankIdAuthResponse.getSSN();
      applyChain = apply(new SSNUpdatedEvent(this.id, ssn));
      // -- Tracking id generation for the new member id
      generateTrackingId();

      try {
        applyChain = getPersonInformationFromBisnode(applyChain, ssn);
      } catch (RuntimeException ex) {
        log.error("Caught exception calling bisnode for personalInformation", ex);
        applyChain =
          applyChain.andThenApply(
            () ->
              new NameUpdatedEvent(
                this.id,
                formatName(bankIdAuthResponse.getGivenName()),
                formatName(bankIdAuthResponse.getSurname())));
      }

      if (this.status == MemberStatus.INITIATED) {
        applyChain =
          applyChain.andThenApply(
            () -> new MemberStartedOnBoardingEvent(this.id, MemberStatus.ONBOARDING));
      }
    }

    MemberAuthenticatedEvent authenticatedEvent =
      new MemberAuthenticatedEvent(this.id, bankIdAuthResponse.getReferenceToken());

    if (applyChain != null) {
      applyChain.andThenApply(() -> authenticatedEvent);
    } else {
      apply(authenticatedEvent);
    }
  }

  private void generateTrackingId() {
    apply(new TrackingIdCreatedEvent(this.id, uuidGenerator.generateRandom()));
  }

  private String formatName(String name) {
    String lowercase = name.toLowerCase();
    return Character.toUpperCase(lowercase.charAt(0)) + lowercase.substring(1);
  }

  private ApplyMore getPersonInformationFromBisnode(ApplyMore applyChain, String ssn)
    throws RuntimeException {
    log.info("Calling bisnode for person information for {}", ssn);
    List<PersonSearchResult> personList = bisnodeClient.match(ssn).getPersons();
    Person person = personList.get(0).getPerson();
    if (personList.size() != 1) {
      throw new RuntimeException("Could not find person at bisnode.");
    }

    applyChain =
      applyChain.andThenApply(
        () -> new NameUpdatedEvent(this.id, person.getPreferredOrFirstName(), person.getFamilyName()));

    BisnodeInformation pi = new BisnodeInformation(ssn, person);
    if (pi.getAddress().isPresent()) {
      applyChain =
        applyChain.andThenApply(
          () -> new LivingAddressUpdatedEvent(this.id, pi.getAddress().get()));
    }
    applyChain.andThenApply(() -> new PersonInformationFromBisnodeEvent(this.id, pi));
    return applyChain;
  }

  @CommandHandler
  void inactivateMember(InactivateMemberCommand command) {
    if (this.status == MemberStatus.INITIATED || this.status == MemberStatus.ONBOARDING) {
      apply(new MemberInactivatedEvent(this.id));
    } else {
      String str =
        String.format("Cannot INACTIAVTE member %s in status: %s", this.id, this.status.name());
      throw new RuntimeException(str);
    }
  }

  @CommandHandler
  void startOnboardingWithSSNCommand(StartOnboardingWithSSNCommand command) {
    if (this.status == MemberStatus.INITIATED || this.status == MemberStatus.ONBOARDING) {
      apply(new OnboardingStartedWithSSNEvent(this.id, command.getSsn()));
      apply(new MemberStartedOnBoardingEvent(this.id, MemberStatus.ONBOARDING));
    } else {
      throw new RuntimeException(
        String.format("Cannot start onboarding in state: %s", this.status));
    }
  }

  @CommandHandler
  void memberUpdateContactInformation(MemberUpdateContactInformationCommand cmd) {
    if (status == MemberStatus.SIGNED) {
      log.error("Will not update member info since member is SIGNED (memberId={})", id);
      return;
    }

    if ((cmd.getFirstName() != null
      && !Objects.equals(this.member.getFirstName(), cmd.getFirstName()))
      || (cmd.getLastName() != null
      && !Objects.equals(this.member.getLastName(), cmd.getLastName()))) {
      apply(new NameUpdatedEvent(this.id, cmd.getFirstName(), cmd.getLastName()));
    }

    if (cmd.getEmail() != null
      && !Objects.equals(member.getEmail(), cmd.getEmail())) {
      apply(new EmailUpdatedEvent(this.id, cmd.getEmail()));
    }

    LivingAddress address = this.member.getLivingAddress();
    if (address == null
      || address.needsUpdate(
      cmd.getStreet(),
      cmd.getCity(),
      cmd.getZipCode(),
      cmd.getApartmentNo(),
      cmd.getFloor())) {
      int floor = (cmd.getFloor() != null) ? cmd.getFloor() : 0;
      apply(
        new LivingAddressUpdatedEvent(
          this.id,
          cmd.getStreet(),
          cmd.getCity(),
          cmd.getZipCode(),
          cmd.getApartmentNo(),
          floor));
    }

    if (cmd.getPhoneNumber() != null
      && !Objects.equals(this.member.getPhoneNumber(), cmd.getPhoneNumber())) {
      apply(new PhoneNumberUpdatedEvent(this.id, cmd.getPhoneNumber()));
    }

    if (cmd.getBirthDate() != null
      && !Objects.equals(this.member.getBirthDate(), cmd.getBirthDate())) {
      apply(new BirthDateUpdatedEvent(this.id, cmd.getBirthDate()));
    }
  }

  @CommandHandler
  void bankIdSignHandler(BankIdSignCommand cmd) {
    if (cmd.getPersonalNumber() != null
      && !Objects.equals(this.member.getSsn(), cmd.getPersonalNumber())) {
      apply(new SSNUpdatedEvent(this.id, cmd.getPersonalNumber()));
    }

    apply(new NewCashbackSelectedEvent(this.id, cashbackService.getDefaultId(member.getPickedLocale()).toString()));


    apply(
      new MemberSignedEvent(
        this.id, cmd.getReferenceId(), cmd.getSignature(), cmd.getOscpResponse(),
        cmd.getPersonalNumber()));

    if (this.trackingId == null) {
      generateTrackingId();
    }
  }

  @CommandHandler
  void ZignSecSignHandler(ZignSecSignCommand cmd) {
    if (!isValidJSON(cmd.getProvideJsonResponse()))
      throw new RuntimeException("Invalid json from provider");

    apply(new NewCashbackSelectedEvent(this.id, cashbackService.getDefaultId(member.getPickedLocale()).toString()));

    ZignSecAuthenticationMarket zignSecAuthMarket = cmd.getZignSecAuthMarket();
    if (zignSecAuthMarket == ZignSecAuthenticationMarket.NORWAY) {
      if (cmd.getPersonalNumber() != null
        && !Objects.equals(this.member.getSsn(), cmd.getPersonalNumber())) {
        apply(new NorwegianSSNUpdatedEvent(this.id, cmd.getPersonalNumber()));
      }

      apply(
        new NorwegianMemberSignedEvent(
          this.id, cmd.getPersonalNumber(), cmd.getProvideJsonResponse(), cmd.getReferenceId()));

      return;
    } else if (zignSecAuthMarket == ZignSecAuthenticationMarket.DENMARK) {
      if (cmd.getPersonalNumber() != null
        && !Objects.equals(this.member.getSsn(), cmd.getPersonalNumber())) {
        apply(new DanishSSNUpdatedEvent(this.id, cmd.getPersonalNumber()));
      }

      apply(
        new DanishMemberSignedEvent(
          this.id, cmd.getPersonalNumber(), cmd.getProvideJsonResponse(), cmd.getReferenceId()));
      return;
    }

    throw new RuntimeException("ZignSec authentication market: " + zignSecAuthMarket.name() + " is not implemented!");
  }

  public boolean isValidJSON(final String json) {
    try {
      objectMapper.readTree(json);
      return true;
    } catch (IOException e) {
      log.error("Failed to validate json", e);
      return false;
    }
  }

  @CommandHandler
  public void on(SignMemberFromUnderwriterCommand signMemberFromUnderwriterCommand) {
    apply(new MemberSignedWithoutBankId(signMemberFromUnderwriterCommand.getId(), signMemberFromUnderwriterCommand.getSsn()));
  }

  @CommandHandler
  void selectNewCashback(SelectNewCashbackCommand cmd) {
    apply(new NewCashbackSelectedEvent(this.id, cmd.getOptionId().toString()));
  }

  @CommandHandler
  void updateEmail(UpdateEmailCommand cmd) {
    apply(new EmailUpdatedEvent(this.id, cmd.getEmail()));
  }

  @CommandHandler
  void editMemberInformation(EditMemberInformationCommand cmd) {
    if ((cmd.getMember().getFirstName() != null
      && !Objects.equals(this.member.getFirstName(), cmd.getMember().getFirstName()))
      || (cmd.getMember().getLastName() != null
      && !Objects.equals(this.member.getLastName(), cmd.getMember().getLastName()))) {
      apply(
        new NameUpdatedEvent(
          this.id, cmd.getMember().getFirstName(), cmd.getMember().getLastName()), MetaData.with("token", cmd.getToken()));
    }

    if (cmd.getMember().getEmail() != null
      && !Objects.equals(member.getEmail(), cmd.getMember().getEmail())) {
      apply(new EmailUpdatedEvent(this.id, cmd.getMember().getEmail()), MetaData.with("token", cmd.getToken()));
    }

    LivingAddress address = this.member.getLivingAddress();
    if (address == null
      || address.needsUpdate(
      cmd.getMember().getStreet(),
      cmd.getMember().getCity(),
      cmd.getMember().getZipCode(),
      cmd.getMember().getApartment(),
      cmd.getMember().getFloor())) {
      apply(
        new LivingAddressUpdatedEvent(
          this.id,
          cmd.getMember().getStreet(),
          cmd.getMember().getCity(),
          cmd.getMember().getZipCode(),
          cmd.getMember().getApartment(),
          cmd.getMember().getFloor()), MetaData.with("token", cmd.getToken()));
    }

    if (cmd.getMember().getPhoneNumber() != null
      && !Objects.equals(this.member.getPhoneNumber(), cmd.getMember().getPhoneNumber())) {
      apply(new PhoneNumberUpdatedEvent(this.id, cmd.getMember().getPhoneNumber()), MetaData.with("token", cmd.getToken()));
    }
  }

  @CommandHandler
  public void on(EditMemberInfoCommand cmd) {
    String firstName = cmd.getFirstName() != null ? cmd.getFirstName() : member.getFirstName();
    String lastName = cmd.getLastName() != null ? cmd.getLastName() : member.getLastName();
    if (!firstName.equals(member.getFirstName()) || !lastName.equals(member.getLastName())) {
      apply(new NameUpdatedEvent(this.id, firstName, lastName), MetaData.with("token", cmd.getToken()));
    }

    if (cmd.getEmail() != null && !cmd.getEmail().equals(member.getEmail())) {
      apply(new EmailUpdatedEvent(this.id, cmd.getEmail()), MetaData.with("token", cmd.getToken()));
    }

    if (cmd.getPhoneNumber() != null && !cmd.getPhoneNumber().equals(member.getPhoneNumber())) {
      apply(new PhoneNumberUpdatedEvent(this.id, cmd.getPhoneNumber()), MetaData.with("token", cmd.getToken()));
    }
  }

  @CommandHandler
  public void on(UpdatePhoneNumberCommand cmd) {
    log.info("Updating phoneNumber for member {}, new number: {}", cmd.getMemberId(),
      cmd.getPhoneNumber());

    if (cmd.getPhoneNumber() != null
      && !Objects.equals(member.getPhoneNumber(), cmd.getPhoneNumber())) {
      apply(new PhoneNumberUpdatedEvent(cmd.getMemberId(), cmd.getPhoneNumber()));
    }
  }

  @CommandHandler
  public void on(SetFraudulentStatusCommand cmd) {
    apply(new FraudulentStatusUpdatedEvent(cmd.getMemberId(), cmd.getFraudulentStatus(), cmd.getFraudulentDescription()), MetaData.with("token", cmd.getToken()));
  }

  @CommandHandler
  public void on(UpdateWebOnBoardingInfoCommand cmd) {
    log.debug("Updating ssn and email for webOnBoarding member {}, ssn: {}, email: {}",
      cmd.getMemberId(), cmd.getSSN(), cmd.getEmail());

    if (cmd.getSSN() != null
      && !Objects.equals(member.getSsn(), cmd.getSSN())) {
      apply(new SSNUpdatedEvent(this.id, cmd.getSSN()));
    }

    if (cmd.getEmail() != null
      && !Objects.equals(member.getEmail(), cmd.getEmail())) {
      apply(new EmailUpdatedEvent(this.id, cmd.getEmail()));
    }
  }

  @CommandHandler
  public void on(UpdateSSNCommand cmd) {
    log.debug("Updating ssn for member {}, ssn: {}", cmd.getMemberId(), cmd.getSSN());
    apply(new SSNUpdatedEvent(cmd.getMemberId(), cmd.getSSN()));
  }

  @CommandHandler
  public void on(InitializeAppleUserCommand cmd) {
    apply(
      new MemberCreatedEvent(
        cmd.getMemberId(),
        MemberStatus.SIGNED)
    );

    apply(
      new SSNUpdatedEvent(
        cmd.getMemberId(),
        cmd.getPersonalNumber()
      )
    );

    apply(
      new NewCashbackSelectedEvent(
        cmd.getMemberId(),
        cashbackService.getDefaultId(member.getPickedLocale()).toString())
    );

    apply(
      new NameUpdatedEvent(
        cmd.getMemberId(),
        cmd.getFirstName(),
        cmd.getLastName()),
      null
    );

    apply(
      new PhoneNumberUpdatedEvent(
        cmd.getMemberId(),
        cmd.getPhoneNumber()
      )
    );

    apply(
      new EmailUpdatedEvent(
        cmd.getMemberId(),
        cmd.getEmail())
    );

    apply(new LivingAddressUpdatedEvent(
        cmd.getMemberId(),
        cmd.getStreet(),
        cmd.getCity(),
        cmd.getZipCode(),
        null,
        0
      )
    );
  }

  @CommandHandler
  public void on(UpdateAcceptLanguageCommand cmd) {
    log.info("Updating accept language for member {}, new number: {}", cmd.getMemberId(), cmd.getAcceptLanguage());

    if (!cmd.getAcceptLanguage().isEmpty() &&
      !Objects.equals(member.getAcceptLanguage(), cmd.getAcceptLanguage())) {
      apply(new AcceptLanguageUpdatedEvent(cmd.getMemberId(), cmd.getAcceptLanguage()));
    }
  }

  @CommandHandler
  public void on(UpdatePickedLocaleCommand cmd) {

    if (!Objects.equals(member.getPickedLocale(), cmd.getPickedLocale())) {
      log.info("Updating picked locale for member {}, new locale: {}", cmd.getMemberId(), cmd.getPickedLocale());

      apply(new PickedLocaleUpdatedEvent(cmd.getMemberId(), cmd.getPickedLocale()));
    }
  }

  @EventSourcingHandler
  public void on(MemberCreatedEvent e) {
    this.id = e.getId();
    this.status = e.getStatus();
    this.member = new Member();
  }

  @EventSourcingHandler
  public void on(MemberStartedOnBoardingEvent e) {
    this.status = e.getNewStatus();
  }

  @EventSourcingHandler
  public void on(NameUpdatedEvent e) {
    this.member.setFirstName(e.getFirstName());
    this.member.setLastName(e.getLastName());
  }

  @EventSourcingHandler
  public void on(PersonInformationFromBisnodeEvent e) {
    this.latestBisnodeInformation = e.getInformation();
  }

  @EventSourcingHandler
  public void on(MemberSignedEvent e) {
    this.status = MemberStatus.SIGNED;
  }

  @EventSourcingHandler
  public void on(NorwegianMemberSignedEvent e) {
    this.status = MemberStatus.SIGNED;
  }

  @EventSourcingHandler
  public void on(DanishMemberSignedEvent e) {
    this.status = MemberStatus.SIGNED;
  }

  @EventSourcingHandler
  public void on(EmailUpdatedEvent e) {
    this.member.setEmail(e.getEmail());
  }

  @EventSourcingHandler
  public void on(LivingAddressUpdatedEvent e) {

    LivingAddress address =
      new LivingAddress(
        e.getStreet(), e.getCity(), e.getZipCode(), e.getApartmentNo(), e.getFloor());
    this.member.setLivingAddress(address);
  }

  @EventSourcingHandler
  public void on(OnboardingStartedWithSSNEvent e) {
    this.member.setSsn(e.getSsn());
  }

  @EventSourcingHandler
  public void on(MemberCancellationEvent e) {
    this.status = MemberStatus.TERMINATED;
  }

  @EventSourcingHandler
  public void on(InsuranceCancellationEvent e) {
    log.info("Cancel insurance with id {} for member {}", e.getInsuranceId(), e.getMemberId());
  }

  @EventHandler
  public void on(TrackingIdCreatedEvent e) {

    this.trackingId = e.getTrackingId();
  }

  @EventSourcingHandler
  public void on(PhoneNumberUpdatedEvent e) {
    this.member.setPhoneNumber(e.getPhoneNumber());
  }

  @EventSourcingHandler
  public void on(BirthDateUpdatedEvent e) {
    this.member.setBirthDate(e.getBirthDate());
  }

  @EventSourcingHandler
  public void on(SSNUpdatedEvent e) {
    this.member.setSsn(e.getSsn());
  }

  @EventSourcingHandler
  public void on(NorwegianSSNUpdatedEvent e) {
    this.member.setSsn(e.getSsn());
  }

  @EventSourcingHandler
  public void on(DanishSSNUpdatedEvent e) {
    this.member.setSsn(e.getSsn());
  }

  @EventSourcingHandler
  public void on(AcceptLanguageUpdatedEvent e) {
    this.member.setAcceptLanguage(e.getAcceptLanguage());
  }

  @EventSourcingHandler
  public void on(PickedLocaleUpdatedEvent e) {
    this.member.setPickedLocale(e.getPickedLocale());
  }
}

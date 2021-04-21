package com.hedvig.memberservice.query;

import com.hedvig.integration.botService.BotService;
import com.hedvig.integration.productsPricing.CampaignService;
import com.hedvig.integration.productsPricing.dto.EditMemberNameRequestDTO;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.events.AcceptLanguageUpdatedEvent;
import com.hedvig.memberservice.events.BirthDateUpdatedEvent;
import com.hedvig.memberservice.events.DanishMemberSignedEvent;
import com.hedvig.memberservice.events.DanishSSNUpdatedEvent;
import com.hedvig.memberservice.events.EmailUpdatedEvent;
import com.hedvig.memberservice.events.FraudulentStatusUpdatedEvent;
import com.hedvig.memberservice.events.LivingAddressUpdatedEvent;
import com.hedvig.memberservice.events.MemberCancellationEvent;
import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.memberservice.events.MemberIdentifiedEvent;
import com.hedvig.memberservice.events.MemberInactivatedEvent;
import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.events.MemberSignedWithoutBankId;
import com.hedvig.memberservice.events.MemberSimpleSignedEvent;
import com.hedvig.memberservice.events.MemberStartedOnBoardingEvent;
import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.hedvig.memberservice.events.NewCashbackSelectedEvent;
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent;
import com.hedvig.memberservice.events.NorwegianSSNUpdatedEvent;
import com.hedvig.memberservice.events.PhoneNumberUpdatedEvent;
import com.hedvig.memberservice.events.PickedLocaleUpdatedEvent;
import com.hedvig.memberservice.events.SSNUpdatedEvent;
import com.hedvig.memberservice.events.TrackingIdCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MemberEventListener {

  private final Logger logger = LoggerFactory.getLogger(MemberEventListener.class);
  private final MemberRepository userRepo;
  private final SignedMemberRepository signedMemberRepository;
  private final TrackingIdRepository trackingRepo;
  private final BotService botService;

  @Autowired
  public MemberEventListener(
      MemberRepository userRepo,
      SignedMemberRepository signedMemberRepository,
      TrackingIdRepository trackingRepo,
      BotService botService
  ) {
    this.userRepo = userRepo;
    this.signedMemberRepository = signedMemberRepository;
    this.trackingRepo = trackingRepo;
    this.botService = botService;
  }

  @EventHandler
  public void on(MemberCreatedEvent e) {
    System.out.println("MemberEventListener: " + e);
    MemberEntity user = new MemberEntity();
    user.setId(e.getId());
    user.setStatus(e.getStatus());
    user.setCreatedOn(e.getCreatedOn());

    userRepo.save(user);
  }

  //    We don't use this event at the moment
  //    @EventHandler
  //    public void on(PersonInformationFromBisnodeEvent e,
  // EventMessage<PersonInformationFromBisnodeEvent> eventMessage) {
  //        logger.debug("Started handling event: {}", eventMessage.getIdentifier());
  //        logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
  //    }

  @EventHandler
  public void on(EmailUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getId()).get();

    m.setEmail(e.getEmail());
    userRepo.save(m);
  }

  @EventHandler
  void on(SSNUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    userRepo.save(m);
  }

  @EventHandler
  void on(NorwegianSSNUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    userRepo.save(m);
  }

  @EventHandler
  void on(DanishSSNUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    userRepo.save(m);
  }

  @EventHandler
  void on(TrackingIdCreatedEvent e) {
    // Assign a unique tracking id per SSN

    trackingRepo
        .findByTrackingId(e.getTrackingId())
        .orElseGet(
            () -> {
              TrackingIdEntity newCampaign = new TrackingIdEntity();
              newCampaign.setMemberId(e.getMemberId());
              newCampaign.setTrackingId(e.getTrackingId());
              trackingRepo.save(newCampaign);
              return newCampaign;
            });
  }

  @EventHandler
  void on(LivingAddressUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getId()).get();

    m.setCity(e.getCity());
    m.setStreet(e.getStreet());
    m.setZipCode(e.getZipCode());
    m.setApartment(e.getApartmentNo());
    m.setFloor(e.getFloor());

    userRepo.save(m);
  }

  @EventHandler
  void on(NameUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();

    m.setFirstName(e.getFirstName());
    m.setLastName(e.getLastName());

    userRepo.save(m);

    EditMemberNameRequestDTO editMemberNameRequestDTO = new EditMemberNameRequestDTO(
      String.valueOf(e.getMemberId()),
      e.getFirstName(),
      e.getLastName()
    );

    CompletableFuture.runAsync(() -> botService.editMemberName(String.valueOf(e.getMemberId()), editMemberNameRequestDTO));
  }

  @EventHandler
  public void on(MemberStartedOnBoardingEvent e, EventMessage<MemberStartedOnBoardingEvent> eventMessage) {
    logger.debug("Started handling event: {}", eventMessage.getIdentifier());

    MemberEntity member = userRepo.findById(e.getMemberId()).get();
    member.setStatus(e.getNewStatus());

    userRepo.saveAndFlush(member);
    logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
  }

  @EventHandler
  void on(MemberInactivatedEvent e) {
    MemberEntity m = userRepo.findById(e.getId()).get();
    m.setStatus(MemberStatus.INACTIVATED);
    userRepo.save(m);
  }

  @EventHandler
  void on(NewCashbackSelectedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setCashbackId(e.getCashbackId());
    userRepo.save(m);
  }

  @EventHandler
  void on(MemberSignedEvent e, @Timestamp Instant timestamp) {
      memberSigned(e.getId(), e.getSsn(), timestamp);
  }

  @EventHandler
  void on(MemberSignedWithoutBankId e, @Timestamp Instant timestamp) {
      memberSigned(e.getMemberId(), e.getSsn(), timestamp);
  }

  @EventHandler
  void on(NorwegianMemberSignedEvent e, @Timestamp Instant timestamp) {
      memberSigned(e.getMemberId(), e.getSsn(), timestamp);
  }

  @EventHandler
  void on(DanishMemberSignedEvent e, @Timestamp Instant timestamp) {
      memberSigned(e.getMemberId(), e.getSsn(), timestamp);
  }

  @EventHandler
  void on(MemberSimpleSignedEvent e, @Timestamp Instant timestamp) {
      memberSigned(e.getMemberId(), e.getNationalIdentification(), timestamp);
  }

  private void memberSigned(Long memberId, String ssn, Instant timestamp) {
      MemberEntity m = userRepo.findById(memberId).get();
      m.setStatus(MemberStatus.SIGNED);
      m.setSignedOn(timestamp);

      SignedMemberEntity sme = new SignedMemberEntity();
      sme.setId(memberId);
      sme.setSsn(ssn);

      userRepo.save(m);
      signedMemberRepository.save(sme);
  }

  @EventHandler
  void on(MemberCancellationEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setStatus(MemberStatus.TERMINATED);

    userRepo.save(m);
  }

  @EventHandler
  void on(PhoneNumberUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getId()).get();
    m.setPhoneNumber(e.getPhoneNumber());

    userRepo.save(m);
  }

  @EventHandler
  void on(BirthDateUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setBirthDate(e.getBirthDate());

    userRepo.save(m);
  }
  @EventHandler
  void on(FraudulentStatusUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setFraudulentStatus(e.getFraudulentStatus());
    m.setFraudulentDescription(e.getFraudulentDescription());
    userRepo.save(m);
  }

  @EventHandler
  void on(AcceptLanguageUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setAcceptLanguage(e.getAcceptLanguage());
    userRepo.save(m);
  }

  @EventHandler
  void on(PickedLocaleUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    m.setPickedLocale(e.getPickedLocale());
    userRepo.save(m);
  }

  void on(@NotNull MemberIdentifiedEvent event) {
      MemberEntity m = userRepo.findById(event.getMemberId()).get();
      m.setFirstName(event.getFirstName());
      m.setLastName(event.getLastName());
      m.setSsn(event.getNationalIdentification().getIdentification());
  }
}

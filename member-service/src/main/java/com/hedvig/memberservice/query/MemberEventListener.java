package com.hedvig.memberservice.query;

import com.hedvig.integration.botService.BotService;
import com.hedvig.integration.productsPricing.CampaignService;
import com.hedvig.integration.productsPricing.dto.EditMemberNameRequestDTO;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.events.*;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MemberEventListener {

  private final Logger logger = LoggerFactory.getLogger(MemberEventListener.class);
  private final MemberRepository memberRepository;
  private final SignedMemberRepository signedMemberRepository;
  private final TrackingIdRepository trackingRepo;
  private final CampaignService campaignService;
  private final BotService botService;

  @Autowired
  public MemberEventListener(
      MemberRepository memberRepository,
      SignedMemberRepository signedMemberRepository,
      TrackingIdRepository trackingRepo,
      CampaignService campaignService,
      BotService botService) {
    this.memberRepository = memberRepository;
    this.signedMemberRepository = signedMemberRepository;
    this.trackingRepo = trackingRepo;
    this.campaignService = campaignService;
    this.botService = botService;
  }

  @EventHandler
  public void on(MemberCreatedEvent e) {
    logger.debug("MemberEventListener: " + e);
    MemberEntity user = new MemberEntity();
    user.setId(e.getId());
    user.setStatus(e.getStatus());
    user.setCreatedOn(e.getCreatedOn());

    memberRepository.save(user);
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
    MemberEntity m = memberRepository.findById(e.getId()).get();

    m.setEmail(e.getEmail());
    memberRepository.save(m);
  }

  @EventHandler
  void on(SSNUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    memberRepository.save(m);
  }

  @EventHandler
  void on(NorwegianSSNUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    memberRepository.save(m);
  }

  @EventHandler
  void on(DanishSSNUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();

    m.setSsn(e.getSsn());

    memberRepository.save(m);
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
    MemberEntity m = memberRepository.findById(e.getId()).get();

    m.setCity(e.getCity());
    m.setStreet(e.getStreet());
    m.setZipCode(e.getZipCode());
    m.setApartment(e.getApartmentNo());
    m.setFloor(e.getFloor());

    memberRepository.save(m);
  }

  @EventHandler
  void on(NameUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();

    m.setFirstName(e.getFirstName());
    m.setLastName(e.getLastName());

    memberRepository.save(m);

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

    MemberEntity member = memberRepository.findById(e.getMemberId()).get();
    member.setStatus(e.getNewStatus());

    memberRepository.saveAndFlush(member);
    logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
  }

  @EventHandler
  void on(MemberInactivatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getId()).get();
    m.setStatus(MemberStatus.INACTIVATED);
    memberRepository.save(m);
  }

  @EventHandler
  void on(NewCashbackSelectedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setCashbackId(e.getCashbackId());
    memberRepository.save(m);
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
      MemberEntity m = memberRepository.findById(memberId).get();
      m.setStatus(MemberStatus.SIGNED);
      m.setSignedOn(timestamp);

      SignedMemberEntity sme = new SignedMemberEntity();
      sme.setId(memberId);
      sme.setSsn(ssn);

      memberRepository.save(m);
      signedMemberRepository.save(sme);
  }

  @EventHandler
  void on(MemberCancellationEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setStatus(MemberStatus.TERMINATED);

    memberRepository.save(m);
  }

  @EventHandler
  void on(PhoneNumberUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getId()).get();
    m.setPhoneNumber(e.getPhoneNumber());

    memberRepository.save(m);
  }

  @EventHandler
  void on(BirthDateUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setBirthDate(e.getBirthDate());

    memberRepository.save(m);
  }
  @EventHandler
  void on(FraudulentStatusUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setFraudulentStatus(e.getFraudulentStatus());
    m.setFraudulentDescription(e.getFraudulentDescription());
    memberRepository.save(m);
  }

  @EventHandler
  void on(AcceptLanguageUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setAcceptLanguage(e.getAcceptLanguage());
    memberRepository.save(m);
  }

  @EventHandler
  void on(PickedLocaleUpdatedEvent e) {
    MemberEntity m = memberRepository.findById(e.getMemberId()).get();
    m.setPickedLocale(e.getPickedLocale());
    memberRepository.save(m);
  }

  @EventHandler
  @Transactional
  void on(MemberDeletedEvent e) {
      if (memberRepository.findById(e.getMemberId()).isPresent()) {
          memberRepository.deleteById(e.getMemberId());
      }
      if (signedMemberRepository.findById(e.getMemberId()).isPresent()) {
          signedMemberRepository.deleteById(e.getMemberId());
      }
      if (trackingRepo.findByMemberId(e.getMemberId()).isPresent()) {
          trackingRepo.deleteById(e.getMemberId());
      }
  }
}

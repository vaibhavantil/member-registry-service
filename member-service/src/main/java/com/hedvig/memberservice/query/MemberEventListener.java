package com.hedvig.memberservice.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.events.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.security.jwt.JwtHelper;


@Component
@Slf4j
public class MemberEventListener {

  private final Logger logger = LoggerFactory.getLogger(MemberEventListener.class);
  private final MemberRepository userRepo;
  private final SignedMemberRepository signedMemberRepository;
  private final TrackingIdRepository trackingRepo;
  private final TraceMemberRepository traceMemberRepository;

  @Autowired
  public MemberEventListener(
      MemberRepository userRepo,
      SignedMemberRepository signedMemberRepository,
      TrackingIdRepository trackingRepo, TraceMemberRepository traceMemberRepository) {
    this.userRepo = userRepo;
    this.signedMemberRepository = signedMemberRepository;
    this.trackingRepo = trackingRepo;
    this.traceMemberRepository = traceMemberRepository;
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

    if (e.getSsn() != null) {
      final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
      LocalDate d = LocalDate.parse(e.getSsn().substring(0, 8), dtf);
      m.setBirthDate(d);
    }

    userRepo.save(m);
  }

  @EventHandler
  void on(TrackingIdCreatedEvent e) {
    // Assign a unique tracking id per SSN
    TrackingIdEntity c =
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
    ;
  }

  @EventHandler
  void on(LivingAddressUpdatedEvent e) {
    MemberEntity m = userRepo.findById(e.getId()).get();

    saveTrace(e.getCity(), m.getCity(), "city", m.getId(), e.getToken());
    saveTrace(e.getStreet(), m.getStreet(), "Street", m.getId(), e.getToken());
    saveTrace(e.getZipCode(), m.getZipCode(), "ZipCode", m.getId(), e.getToken());
    saveTrace(e.getApartmentNo(), m.getApartment(), "Apartment", m.getId(), e.getToken());
    saveTrace(""+e.getFloor(), ""+m.getFloor(), "Floor", m.getId(), e.getToken());

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

    saveTrace(e.getFirstName(), m.getFirstName(), "FirstName", m.getId(), e.getToken());
    saveTrace(e.getLastName(), m.getLastName(), "LastName", m.getId(), e.getToken());

    m.setFirstName(e.getFirstName());
    m.setLastName(e.getLastName());

    userRepo.save(m);
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
    MemberEntity m = userRepo.findById(e.getId()).get();
    m.setStatus(MemberStatus.SIGNED);
    m.setSignedOn(timestamp);

    SignedMemberEntity sme = new SignedMemberEntity();
    sme.setId(e.getId());
    sme.setSsn(e.getSsn());

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
    saveTrace(e.getPhoneNumber(), m.getPhoneNumber(), "PhoneNumber", m.getId(), e.getToken());
    m.setPhoneNumber(e.getPhoneNumber());

    userRepo.save(m);
  }

  @EventHandler
  void on(FraudulentStatusEvent e) {
    MemberEntity m = userRepo.findById(e.getMemberId()).get();
    saveTrace(e.getFraudulentStatus().name(), m.getFraudulentStatus()!=null?m.getFraudulentStatus().name():"", "fraudulentStatus", m.getId(), e.getToken());
    saveTrace(e.getFraudulentDescription(), m.getFraudulentDescription(), "FraudulentDescription", m.getId(), e.getToken());
    m.setFraudulentStatus(e.getFraudulentStatus());
    m.setFraudulentDescription(e.getFraudulentDescription());
    userRepo.save(m);
  }


  private void saveTrace (String newValue, String oldValue, String fieldName, Long memberId, String token) {
    if (oldValue!= null && !oldValue.equals(newValue) || oldValue== null && newValue!=null) {
      TraceMemberEntity traceMemberEntity = new TraceMemberEntity();
      traceMemberEntity.setDate(LocalDateTime.now());
      traceMemberEntity.setFieldName(fieldName);
      traceMemberEntity.setNewValue(newValue);
      traceMemberEntity.setOldValue(oldValue);
      traceMemberEntity.setMemberId(memberId);
      traceMemberEntity.setUserId(getUserId(token));

      traceMemberRepository.save(traceMemberEntity);
    }
  }

  private String getUserId (String token) {
    try {
      Map<String, String> map = (new ObjectMapper().readValue(JwtHelper.decode(token).getClaims(), new TypeReference<Map<String, String>>() {}));
      return map.get("email");
    } catch (IOException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}

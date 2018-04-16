package com.hedvig.memberservice.query;

import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.events.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MemberEventListener {

    private final Logger logger = LoggerFactory.getLogger(MemberEventListener.class);
    private final MemberRepository userRepo;
    private final SignedMemberRepository signedMemberRepository;

    @Autowired
    public MemberEventListener(MemberRepository userRepo, SignedMemberRepository signedMemberRepository) {
        this.userRepo = userRepo;
        this.signedMemberRepository = signedMemberRepository;
    }

    @EventHandler
    public void on(MemberCreatedEvent e){
        System.out.println("MemberEventListener: " + e);
        MemberEntity user = new MemberEntity();
        user.setId( e.getId());
        user.setStatus(e.getStatus().name());

        userRepo.save(user);
    }

//    We don't use this event at the moment
//    @EventHandler
//    public void on(PersonInformationFromBisnodeEvent e, EventMessage<PersonInformationFromBisnodeEvent> eventMessage) {
//        logger.debug("Started handling event: {}", eventMessage.getIdentifier());
//        logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
//    }

    @EventHandler
    public void on(EmailUpdatedEvent e) {
        MemberEntity m = userRepo.findOne(e.getId());

        m.setEmail(e.getEmail());
        userRepo.save(m);
    }

    @EventHandler void on(SSNUpdatedEvent e) {
        MemberEntity m = userRepo.findOne(e.getMemberId());

        m.setSsn(e.getSsn());

        if(e.getSsn() != null) {
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate d = LocalDate.parse(e.getSsn().substring(0, 8), dtf);
            m.setBirthDate(d);
        }

        userRepo.save(m);
    }

    @EventHandler void on(LivingAddressUpdatedEvent e) {
        MemberEntity m = userRepo.findOne(e.getId());

        m.setCity(e.getCity());
        m.setStreet(e.getStreet());
        m.setZipCode(e.getZipCode());
        m.setApartment(e.getApartmentNo());
        m.setFloor(e.getFloor());

        userRepo.save(m);
    }

    @EventHandler
    void on (NameUpdatedEvent e) {
        MemberEntity m = userRepo.findOne(e.getMemberId());

        m.setFirstName(e.getFirstName());
        m.setLastName(e.getLastName());

        userRepo.save(m);
    }

    @EventHandler
    public void on(MemberStartedOnBoardingEvent e, EventMessage<MemberStartedOnBoardingEvent> eventMessage) {
        logger.debug("Started handling event: {}", eventMessage.getIdentifier());

        MemberEntity member = userRepo.findOne(e.getMemberId());
        member.setStatus(e.getNewStatus().name());

        userRepo.saveAndFlush(member);
        logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
    }

    @EventHandler
    void on(MemberInactivatedEvent e){
        MemberEntity m = userRepo.findOne(e.getId());
        m.setStatus(MemberStatus.INACTIVATED.name());
        userRepo.save(m);
    }

    @EventHandler
    void on(NewCashbackSelectedEvent e){
        MemberEntity m = userRepo.findOne(e.getMemberId());
        m.setCashbackId(e.getCashbackId());
        userRepo.save(m);
    }

    @EventHandler
    void on(MemberSignedEvent e){
        MemberEntity m = userRepo.findOne(e.getId());
        m.setStatus(MemberStatus.SIGNED.name());

        SignedMemberEntity sme = new SignedMemberEntity();
        sme.setId(e.getId());
        sme.setSsn(m.getSsn());

        userRepo.save(m);
        signedMemberRepository.save(sme);
    }


}

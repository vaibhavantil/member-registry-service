package com.hedvig.memberservice.query;

import com.hedvig.botService.web.dto.MemberAuthedEvent;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.aggregates.PersonInformation;
import com.hedvig.memberservice.aggregates.Telephone;
import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.memberservice.events.MemberInactivatedEvent;
import com.hedvig.memberservice.events.MemberStartedOnBoardingEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MemberEventListener {

    private final Logger logger = LoggerFactory.getLogger(MemberEventListener.class);
    private final MemberRepository userRepo;

    @Autowired
    public MemberEventListener(MemberRepository userRepo) {
        this.userRepo = userRepo;
    }

    @EventHandler
    public void on(MemberCreatedEvent e){
        System.out.println("MemberEventListener: " + e);
        MemberEntity user = new MemberEntity();
        user.setId( e.getId());
        user.setStatus(e.getStatus().name());

        userRepo.save(user);
    }

    @EventHandler
    public void on(MemberStartedOnBoardingEvent e, EventMessage<MemberStartedOnBoardingEvent> eventMessage) {
        logger.debug("Started handling event: {}", eventMessage.getIdentifier());

        MemberEntity member = userRepo.findOne(e.getMemberId());

        member.setStatus(e.getNewStatus().name());
        PersonInformation personInformation = e.getPersonInformation();
        member.setSsn(personInformation.getSsn());
        member.setFirstName(personInformation.getPreferredFirstName());
        member.setLastName(personInformation.getFamilyName());
        member.setBirthDate(personInformation.getDateOfBirth());

        personInformation.getAddress().ifPresent( address -> {
            member.setStreetName(address.getStreetName());
            member.setStreetNumber(address.getStreetNumber());
            member.setEntrance(address.getEntrance());
            member.setPostalCode(address.getPostalCode());
            member.setCity(address.getCity());
        });

        List<Telephone> phoneList = personInformation.getPhoneList();
        if(phoneList.size() > 0) {
            Telephone main = phoneList.get(0);
            member.setPhoneNumber(main.getNumber());
        }

        userRepo.saveAndFlush(member);

        logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
    }

    @EventHandler
    void on(MemberInactivatedEvent e){
        MemberEntity m = userRepo.findOne(e.getId());
        m.setStatus(MemberStatus.INACTIVATED.name());
    }

}

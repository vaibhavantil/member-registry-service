package com.hedvig.generic.customerregistry.query;

import com.hedvig.generic.customerregistry.aggregates.Address;
import com.hedvig.generic.customerregistry.aggregates.PersonInformation;
import com.hedvig.generic.customerregistry.aggregates.Telephone;
import com.hedvig.generic.customerregistry.commands.StartOnBoardingCommand;
import com.hedvig.generic.customerregistry.events.MemberCreatedEvent;
import com.hedvig.generic.customerregistry.events.MemberStartedOnBoarding;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MemberEventListener {

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
    public void on(MemberStartedOnBoarding e) {

        MemberEntity member = userRepo.findOne(e.getMemberId());

        member.setStatus(e.getNewStatus().name());
        PersonInformation personInformation = e.getPersonInformation();
        member.setSsn(personInformation.getSsn());
        member.setPreferredName(personInformation.getPreferredFirstName());
        member.setFullName(personInformation.getFullName());
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

        userRepo.save(member);

    }
}

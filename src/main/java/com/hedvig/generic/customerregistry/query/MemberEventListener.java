package com.hedvig.generic.customerregistry.query;

import com.hedvig.generic.customerregistry.events.MemberCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberEventListener {

    private final MemberRepository userRepo;

    @Autowired
    public MemberEventListener(MemberRepository userRepo) {
        this.userRepo = userRepo;
    }

    /*@EventHandler
    public void on(MemberCreatedEvent e){
        System.out.println("MemberEventListener: " + e);
        MemberEntity user = new MemberEntity();
        user.setId( e.getId());
        user.name = e.getName();
        user.birthDate = e.getBirthDate();

        userRepo.save(user);
    }*/
}

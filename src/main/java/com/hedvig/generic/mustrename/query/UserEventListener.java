package com.hedvig.generic.mustrename.query;

import com.hedvig.generic.mustrename.events.UserCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final UserRepository userRepo;

    @Autowired
    public UserEventListener(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @EventHandler
    public void on(UserCreatedEvent e){
        System.out.println("UserEventListener: " + e);
        UserEntity user = new UserEntity();
        user.id = e.getId();
        user.name = e.getName();
        user.birthDate = e.getBirthDate();

        userRepo.save(user);
    }
}

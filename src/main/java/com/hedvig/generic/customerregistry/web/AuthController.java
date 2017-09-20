package com.hedvig.generic.mustrename.web;

import com.hedvig.generic.mustrename.web.dto.AuthResponse;
import com.hedvig.generic.mustrename.web.dto.BankIdAuthRequest;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/auth")
public class AuthController {

    private final CommandGateway commandBus;

    @Autowired
    public AuthController(CommandBus commandBus) {
        this.commandBus = new DefaultCommandGateway(commandBus);
    }

    @RequestMapping(path="bankId")
    public ResponseEntity<AuthResponse> index(@RequestBody BankIdAuthRequest authRequest) {
        //If customer exists, auth with billecta.
        //If customer does not exists, auth then add.
        //Assert customerExists in our database.
        //Call Billectea!
        if(Math.random() % 10 == 2){
            return ResponseEntity.ok(new AuthResponse("h:123"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}

package com.hedvig.generic.mustrename.web;

import com.hedvig.generic.mustrename.commands.CreateUserCommand;
import com.hedvig.generic.mustrename.query.UserRepository;
import com.hedvig.generic.mustrename.web.dto.UserDTO;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final CommandGateway commandBus;

    @Autowired
    public UserController(CommandBus commandBus, UserRepository repository) {
        this.commandBus = new DefaultCommandGateway(commandBus);
        this.userRepository = repository;
    }

    @RequestMapping(path="/user/{userId}")
    public ResponseEntity<UserDTO> index(@PathVariable String userId) {
        return userRepository
                .findById(userId)
                .map(u -> ResponseEntity.ok(new UserDTO(u.id, u.name, u.birthDate)))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/user/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody UserDTO user) {
        UUID uid = UUID.randomUUID();
        System.out.println(uid.toString());
        commandBus.sendAndWait(new CreateUserCommand(uid.toString(), user.name, user.birthDate));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(uid.toString()).toUri();
        return ResponseEntity.created(location).build();
    }

}

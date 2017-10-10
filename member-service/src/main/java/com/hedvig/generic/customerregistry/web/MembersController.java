package com.hedvig.generic.customerregistry.web;

import com.hedvig.generic.customerregistry.commands.ConvertAfterBankIdAuthCommand;
import com.hedvig.generic.customerregistry.commands.CreateMemberCommand;
import com.hedvig.generic.customerregistry.query.MemberEntity;
import com.hedvig.generic.customerregistry.query.MemberRepository;
import com.hedvig.generic.customerregistry.web.dto.Member;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController()
@RequestMapping("/v1/members/")
public class MembersController {

    private final MemberRepository repo;
    private final CommandGateway commandGateway;
    private final RetryTemplate retryTemplate;
    private final SecureRandom randomGenerator;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public MembersController(MemberRepository repo,
                             CommandGateway commandGateway,
                             RetryTemplate retryTemplate) throws NoSuchAlgorithmException {
        this.repo = repo;
        this.commandGateway = commandGateway;
        this.retryTemplate = retryTemplate;
        this.randomGenerator = SecureRandom.getInstance("SHA1PRNG");
    }

    @RequestMapping("/{memberId}")
    public ResponseEntity<Member> index(@PathVariable String memberId) {
        return ResponseEntity.ok(new Member("","","","","","","","",""));
    }

    @RequestMapping("/helloHedvig")
    public ResponseEntity<String> start() throws Exception {


        Long id = retryTemplate.execute(arg -> {
            Long memberId;
            Optional<MemberEntity> member;
            do {
                memberId = Math.abs(this.randomGenerator.nextLong() % 1000000000);
                member = repo.findById(memberId);
            }while(member.isPresent());

            CompletableFuture<Object>  a = commandGateway.send(new CreateMemberCommand(memberId));
            Object ret = a.get();
            log.info(ret.toString());
            return memberId;
        });

        return ResponseEntity.ok("{\"memberId\":" + id + "\"}");
    }

    @RequestMapping("/convert")
    public ResponseEntity<String> convert(@RequestParam String personalIdentificationNumber,
                                          @RequestParam Long memberId,
                                          @RequestParam String givenName,
                                          @RequestParam String surName,
                                          @RequestParam String name
    ) throws ExecutionException, InterruptedException {
        Optional<MemberEntity> member = repo.findBySsn(personalIdentificationNumber);
        if(member.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(member.get().getId().toString());
        }

        Future f = commandGateway.send(new ConvertAfterBankIdAuthCommand(memberId, personalIdentificationNumber, givenName, surName, name));
        f.get();
        return ResponseEntity.ok("");
    }

}

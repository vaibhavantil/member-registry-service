package com.hedvig.memberservice.web;

import com.hedvig.memberservice.commands.ConvertAfterBankIdAuthCommand;
import com.hedvig.memberservice.commands.CreateMemberCommand;
import com.hedvig.memberservice.externalApi.prouctsPricing.ProductApi;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.Member;
import com.hedvig.memberservice.web.dto.Profile;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController()
@RequestMapping("/member/")
public class MembersController {

    private final MemberRepository repo;
    private final CommandGateway commandGateway;
    private final RetryTemplate retryTemplate;
    private final SecureRandom randomGenerator;
    private final ProductApi productApi;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public MembersController(MemberRepository repo,
                             CommandGateway commandGateway,
                             RetryTemplate retryTemplate, ProductApi productApi) throws NoSuchAlgorithmException {
        this.repo = repo;
        this.commandGateway = commandGateway;
        this.retryTemplate = retryTemplate;
        this.productApi = productApi;
        this.randomGenerator = SecureRandom.getInstance("SHA1PRNG");
    }

    @RequestMapping("/{memberId}")
    public ResponseEntity<Member> index(@PathVariable Long memberId) {

        Optional<MemberEntity> member = repo.findById(memberId);
        if(member.isPresent()){

            return ResponseEntity.ok(new Member(member.get()));
        }

        return ResponseEntity.notFound().build();
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

        log.info("New member created with id: " + id);
        return ResponseEntity.ok("{\"memberId\":" + id + "}");
    }

    @RequestMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "hedvig.token", required = false) Long hid){
        Optional<MemberEntity> m = repo.findById(hid);
        if(!m.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message:\":\"Member not found.\"");
        }

        MemberEntity me = m.get();
        
        Profile p = new Profile(
                String.format("%s %s", me.getFirstName(), me.getLastName()),
                me.getFirstName(),
                me.getLastName(),
                new ArrayList<>(),
                me.getBirthDate()==null?null:me.getBirthDate().until(LocalDate.now()).getYears(),
                me.getEmail(),
                me.getStreet(),
                0,
                "XXXX XXXX 1234",
                "Rädda Barnen",
                "ACTIVE",
                LocalDate.now().withDayOfMonth(25),
                String.format("\"Tack kära %s för att du bidrar till att rädda livet på fler cancerdrabbade barn\"", me.getFirstName()),
                productApi.getSafetyIncreasers(hid)
                );

        return ResponseEntity.ok(p);
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

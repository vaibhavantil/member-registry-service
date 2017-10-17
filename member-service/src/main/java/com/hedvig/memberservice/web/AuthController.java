package com.hedvig.memberservice.web;

import com.hedvig.botService.web.dto.MemberAuthedEvent;
import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdStatusType;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.external.bisnodeBCI.dto.Person;
import com.hedvig.external.bisnodeBCI.dto.PersonSearchResult;
import com.hedvig.memberservice.commands.StartOnBoardingCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.BankIdAuthResponse;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController()
@RequestMapping("/member/bankid/")
public class AuthController {

    private final CommandGateway commandBus;
    private final BillectaApi billectaApi;
    private final MemberRepository memberRepo;
    private final BisnodeClient bisnodeClient;
    private final RestTemplate restTemplate;
    private static Logger log = LoggerFactory.getLogger(AuthController.class);

    @Value("${hedvig.bot-service.url}")
    private String botServiceUrl;

    @Autowired
    public AuthController(CommandBus commandBus,
                          BillectaApi billectaApi,
                          MemberRepository memberRepo,
                          BisnodeClient bisnodeClient,
                          RestTemplate restTemplate) {
        this.commandBus = new DefaultCommandGateway(commandBus);
        this.billectaApi = billectaApi;
        this.memberRepo = memberRepo;
        this.bisnodeClient = bisnodeClient;
        this.restTemplate = restTemplate;
    }

    @PostMapping(path = "auth")
    public ResponseEntity<BankIdAuthResponse> auth(@RequestParam(required = false) String ssn) {


        BankIdAuthenticationStatus status = billectaApi.BankIdAuth(Optional.of(ssn));

        BankIdAuthResponse response = null;
        if (status.getStatus() == BankIdStatusType.STARTED) {
            response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken());
        } else {
            response = new BankIdAuthResponse(status.getStatus(), "", "");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "collect")
    public ResponseEntity<BankIdAuthResponse> collect(@RequestParam String referenceToken, @RequestHeader(value = "hedvig.token", required = false) Long hid) {

        BankIdAuthenticationStatus status = billectaApi.BankIdCollect(referenceToken);
        BankIdAuthResponse response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken());

        if (status.getStatus() == BankIdStatusType.COMPLETE) {
            String ssn = status.getSSN();

            Optional<MemberEntity> member = memberRepo.findBySsn(ssn);
            sendAuthEvent(member.map(m -> m.getId()).orElse(hid));



            return member
                    //If we already have a member with this SSN return his hedvigId.
                    .map(m ->
                            ResponseEntity
                                    .status(HttpStatus.OK)
                                    .header("Hedvig.Id", m.getId().toString())
                                    .body(response))
                    //Add personal information to member
                    .orElseGet(() ->
                    {
                        List<PersonSearchResult> personList = bisnodeClient.match(ssn).getPersons();
                        if (personList.size() != 1) {
                            log.error("Could not find person based on personnumer!");
                            throw new RuntimeException(("Could not find person at bisnode."));
                        }

                        Person person = personList.get(0).getPerson();

                        StartOnBoardingCommand cmd = new StartOnBoardingCommand(
                                hid,
                                status,
                                person
                        );

                        commandBus.sendAndWait(cmd);

                        return ResponseEntity.status(HttpStatus.OK).body(response);

                    });
        }

        return ResponseEntity.ok(response);
    }

    private void sendAuthEvent(Long memberId) {
        MemberAuthedEvent authedEvent = new MemberAuthedEvent(memberId);
        //RestTemplate template = new RestTemplate();
        //Boolean development = Arrays.stream(springEnvironment.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("development"));



        HttpEntity<String> response = restTemplate.postForEntity(
                "{url}/event/memberservice",
                authedEvent,
                String.class,
                botServiceUrl);
    }
}
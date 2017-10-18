package com.hedvig.memberservice.web;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdStatusType;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.aggregates.exceptions.BankIdReferenceUsedException;
import com.hedvig.memberservice.commands.AuthenticationAttemptCommand;
import com.hedvig.memberservice.commands.InactivateMemberCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.BankIdAuthRequest;
import com.hedvig.memberservice.web.dto.BankIdAuthResponse;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    public ResponseEntity<BankIdAuthResponse> auth(@RequestBody(required = true) BankIdAuthRequest request) {

        BankIdAuthenticationStatus status = billectaApi.BankIdAuth(request.getSsn());

        BankIdAuthResponse response = null;
        if (status.getStatus() == BankIdStatusType.STARTED) {
            response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken());
        } else {
            response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "collect")
    public ResponseEntity<?> collect(@RequestParam String referenceToken, @RequestHeader(value = "hedvig.token", required = false) Long hid) {

        BankIdAuthenticationStatus status = billectaApi.BankIdCollect(referenceToken);
        BankIdAuthResponse response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken());

        if (status.getStatus() == BankIdStatusType.COMPLETE) {
            String ssn = status.getSSN();

            Optional<MemberEntity> member = memberRepo.findBySsn(ssn);

            Long currentMemberId = hid;
            if(member.isPresent()) {
                MemberEntity m = member.get();
                if(!m.getId().equals(hid)) {
                    this.commandBus.sendAndWait(new InactivateMemberCommand(hid));
                }
                currentMemberId = m.getId();
            }

            try {
                this.commandBus.sendAndWait(new AuthenticationAttemptCommand(currentMemberId, status));
            }catch (BankIdReferenceUsedException e) {
                log.info("Old reference token used: ", e);
                return ResponseEntity.badRequest().body("{\"message\":\"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.ok().header("Hedvig.Id", currentMemberId.toString()).body(response);
        }

        return  ResponseEntity.ok(response);
    }
}
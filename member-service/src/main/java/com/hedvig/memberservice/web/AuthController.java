package com.hedvig.memberservice.web;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdSignStatus;
import com.hedvig.external.billectaAPI.api.BankIdStatusType;
import com.hedvig.memberservice.aggregates.exceptions.BankIdReferenceUsedException;
import com.hedvig.memberservice.commands.AuthenticationAttemptCommand;
import com.hedvig.memberservice.commands.BankIdSignCommand;
import com.hedvig.memberservice.commands.InactivateMemberCommand;
import com.hedvig.memberservice.query.*;
import com.hedvig.memberservice.web.dto.BankIdAuthRequest;
import com.hedvig.memberservice.web.dto.BankIdAuthResponse;
import com.hedvig.memberservice.web.dto.BankIdSignRequest;
import com.hedvig.memberservice.web.dto.BankIdSignResponse;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController()
@RequestMapping("/member/bankid/")
public class AuthController {

    private final CommandGateway commandBus;
    private final BillectaApi billectaApi;
    private final MemberRepository memberRepo;
    private final SignedMemberRepository signedMemberRepository;
    private final CollectRepository collectRepo;
    private static Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(CommandBus commandBus,
                          BillectaApi billectaApi,
                          MemberRepository memberRepo,
                          SignedMemberRepository signedMemberRepository, CollectRepository collectRepo) {
        this.commandBus = new DefaultCommandGateway(commandBus);
        this.billectaApi = billectaApi;
        this.memberRepo = memberRepo;
        this.signedMemberRepository = signedMemberRepository;
        this.collectRepo = collectRepo;
    }

    @PostMapping(path = "auth")
    public ResponseEntity<BankIdAuthResponse> auth(@RequestBody(required = true) BankIdAuthRequest request) {

        long memberId = convertMemberId(request.getMemberId());

        BankIdAuthenticationStatus status = billectaApi.BankIdAuth(request.getSsn());
        log.info("Started bankId AUTH memberId:{}, autostart:{}, reference:{}, ssn:{}",
                status.getStatus().value(),
                status.getAutoStartToken(),
                status.getReferenceToken(),
                status.getSSN());
        BankIdAuthResponse response = null;
        if (status.getStatus() == BankIdStatusType.STARTED) {
            response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken(), null);
        } else {
            response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken(), null);
        }

        trackAuthToken(response.getReferenceToken(), memberId);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path ="sign")
    public ResponseEntity<BankIdSignResponse> sign(@RequestBody(required = true) BankIdSignRequest request) {
        long memberId = convertMemberId(request.getMemberId());

        BankIdSignStatus status = billectaApi.BankIdSign(request.getSsn(), request.getUserMessage());
        BankIdSignResponse response = new BankIdSignResponse(status.getAutoStartToken(), status.getReferenceToken(), status.getStatus().value());

        trackSignToken(response.getReferenceToken(), memberId);

        return ResponseEntity.ok(response);
    }

    private long convertMemberId(String memberId) {
        try {
            return Long.parseLong(memberId);
        }catch(Exception e) {
            throw new HttpMessageNotReadableException("Could not parse memberId");
        }
    }

    private void trackSignToken(String referenceToken, Long memberId) {
        trackReferenceToken(referenceToken, CollectType.RequestType.SIGN, memberId);
    }

    private void trackAuthToken(String referenceToken, Long memberId) {
        trackReferenceToken(referenceToken, CollectType.RequestType.AUTH, memberId);
    }

    private void trackReferenceToken(String referenceToken, CollectType.RequestType sign, Long memberId) {
        CollectType ct = new CollectType();
        ct.token = referenceToken;
        ct.type = sign;
        ct.memberId = memberId;
        collectRepo.save(ct);
    }

    @PostMapping(path = "collect")
    public ResponseEntity<?> collect(@RequestParam String referenceToken, @RequestHeader(value = "hedvig.token") Long hid) throws InterruptedException {

        CollectType collectType = collectRepo.findOne(referenceToken);
        BankIdAuthResponse response;
        
        if(collectType==null){
        	log.error("ERROR: Oh no! Collect type is null!");
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        if(collectType.type.equals(CollectType.RequestType.AUTH)) {
            BankIdAuthenticationStatus status = billectaApi.BankIdCollect(referenceToken);


            if (status.getStatus() == BankIdStatusType.COMPLETE) {
                String ssn = status.getSSN();

                Optional<SignedMemberEntity> member = signedMemberRepository.findBySsn(ssn);

                Long currentMemberId = hid;
                if (member.isPresent()) {
                    SignedMemberEntity m = member.get();
                    if (!m.getId().equals(hid)) {
                        this.commandBus.sendAndWait(new InactivateMemberCommand(hid));
                    }
                    currentMemberId = m.getId();
                }

                try {
                    this.commandBus.sendAndWait(new AuthenticationAttemptCommand(currentMemberId, status));
                    Thread.sleep(1000l);
                } catch (BankIdReferenceUsedException e) {
                    log.info("Old reference token used: ", e);
                    return ResponseEntity.badRequest().body("{\"message\":\"" + e.getMessage() + "\"}");
                }

                response = new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken(), Objects.toString(currentMemberId));

                return ResponseEntity.ok().header("Hedvig.Id", currentMemberId.toString()).body(response);
            }

            return ResponseEntity.ok(new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken(), hid.toString()));

        } else if (collectType.type.equals(CollectType.RequestType.SIGN)) {
            BankIdSignStatus status = billectaApi.bankIdSignCollect(referenceToken);
            if(status.getStatus() == BankIdStatusType.COMPLETE || status.getStatus() == BankIdStatusType.ERROR) {
                Optional<MemberEntity> memberEntity = memberRepo.findById(hid);
                if (memberEntity.isPresent()) {
                    //if (memberEntity.get().getId().equals(hid)) {
                        this.commandBus.sendAndWait(new BankIdSignCommand(hid, status.getReferenceToken()));
                    //}
                }
            }
            return ResponseEntity.ok(new BankIdAuthResponse(status.getStatus(), status.getAutoStartToken(), status.getReferenceToken(), hid.toString()));
        } else {

            return ResponseEntity.noContent().build();
        }
    }
}
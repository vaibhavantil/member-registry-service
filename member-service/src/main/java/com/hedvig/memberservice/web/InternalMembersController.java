package com.hedvig.memberservice.web;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.externalApi.BotService;
import com.hedvig.memberservice.query.CollectRepository;
import com.hedvig.memberservice.query.CollectType;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.InternalMember;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import com.hedvig.memberservice.web.dto.events.BankAccountRetrievalFailed;
import com.hedvig.memberservice.web.dto.events.BankAccountRetrievalSuccess;
import com.hedvig.memberservice.web.dto.events.MemberServiceEvent;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/i/member")
public class InternalMembersController {

    private final CommandGateway commandBus;
    private final BillectaApi billectaApi;
    private final MemberRepository memberRepository;
    private final BotService botSerivce;
    private final CollectRepository collectRepository;

    public InternalMembersController(
            CommandGateway commandBus,
            BillectaApi billectaApi,
            MemberRepository memberRepository,
            BotService botSerivce,
            CollectRepository collectRepository) {
        this.commandBus = commandBus;
        this.billectaApi = billectaApi;
        this.memberRepository = memberRepository;
        this.botSerivce = botSerivce;
        this.collectRepository = collectRepository;
    }


    @RequestMapping("/{memberId}/startBankAccountRetrieval/{bankId}")
    public ResponseEntity<String> startBankAccountRetrieval(@PathVariable Long memberId, @PathVariable String bankId) {
        MemberEntity me = memberRepository.findOne(memberId);

        String publicId = billectaApi.retrieveBankAccountNumbers(
                me.getSsn(),
                bankId,
                accounts -> {

                    BankAccountRetrievalSuccess details = new BankAccountRetrievalSuccess(accounts);

                    MemberServiceEvent e = new MemberServiceEvent(me.getId(), Instant.now(), details);

                    botSerivce.sendEvent(e);
                },
                errorMsg -> {
                    BankAccountRetrievalFailed payload = new BankAccountRetrievalFailed(errorMsg);

                    MemberServiceEvent e = new MemberServiceEvent(me.getId(), Instant.now(), payload);
                    botSerivce.sendEvent(e);
                }
        );

        CollectType ct = new CollectType();
        ct.token = publicId;
        ct.type = CollectType.RequestType.RETRIEVE_ACCOUNTS;
        this.collectRepository.save(ct);

        return ResponseEntity.ok("{\"id\":\"" + publicId + "\"}");
    }

    @RequestMapping(value = "/{memberId}/finalizeOnboarding", method = RequestMethod.POST)
    public ResponseEntity<?> finalizeOnboarding(@PathVariable Long memberId, @RequestBody UpdateContactInformationRequest body) {

        MemberUpdateContactInformationCommand finalizeOnBoardingCommand = new MemberUpdateContactInformationCommand(memberId, body);

        commandBus.sendAndWait(finalizeOnBoardingCommand);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{memberId}/startOnboardingWithSSN", method = RequestMethod.POST)
    public ResponseEntity<?> startOnboardingWithSSN(@PathVariable Long memberId, @RequestBody StartOnboardingWithSSNRequest request) {


        Optional<MemberEntity> member = memberRepository.findBySsn(request.getSsn());
        if (member.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(member.get().getId().toString());
        }

        commandBus.sendAndWait(new StartOnboardingWithSSNCommand(memberId, request));

        return ResponseEntity.noContent().build();
    }


    @RequestMapping(value = "/{memberId}/updateContactInformationRequest", method = RequestMethod.POST)
    public ResponseEntity<?> startOnBoarding(@PathVariable Long memberId, @RequestBody UpdateContactInformationRequest request) {
        commandBus.sendAndWait(new MemberUpdateContactInformationCommand(memberId, request));

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @Transactional
    public Iterator<InternalMember> searchMembers(
            @RequestParam(name = "status", defaultValue = "", required = false) String status,
            @RequestParam(name = "query", defaultValue = "", required = false) String query) {

        status = status.trim();
        query = query.trim();
        if (!query.equals("")) {
            query = "%" + query + "%";
        }
        try (val stream = search(status, query)) {
            return stream
                    .map(InternalMember::fromEnity)
                    .iterator();
        }
    }

    private Stream<MemberEntity> search(String status, String query) {
        if (status.equals("") && query.equals("")) {
            return memberRepository.searchAll();
        }
        if (!status.equals("")) {
            return memberRepository.searchByStatus(status);
        }
        if (!query.equals("")) {
            return memberRepository.searchByQuery(query);
        }
        return memberRepository.searchByStatusAndQuery(status, query);
    }
}

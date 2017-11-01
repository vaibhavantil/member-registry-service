package com.hedvig.memberservice.web;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.memberservice.externalApi.BotService;
import com.hedvig.memberservice.query.CollectRepository;
import com.hedvig.memberservice.query.CollectType;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.events.BankAccountRetrievalFailed;
import com.hedvig.memberservice.web.dto.events.BankAccountRetrievalSuccess;
import com.hedvig.memberservice.web.dto.events.MemberServiceEvent;
import org.axonframework.commandhandling.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/i/member")
public class InternalMemberController {

    private final BillectaApi billectaApi;
    private final MemberRepository memberRepository;
    private final BotService botSerivce;
    private final CollectRepository collectRepository;

    public InternalMemberController(CommandBus commandBus,
                                    BillectaApi billectaApi,
                                    MemberRepository memberRepository,
                                    BotService botSerivce,
                                    CollectRepository collectRepository) {

        this.billectaApi = billectaApi;
        this.memberRepository = memberRepository;
        this.botSerivce = botSerivce;
        this.collectRepository = collectRepository;
    }


    @RequestMapping("/{memberId}/startBankAccountRetrieval/{bankId}")
    public ResponseEntity<String> startBankAccountRetrieval(@PathVariable Long memberId,@PathVariable String bankId) {
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

        return ResponseEntity.ok("{\"id\":\"" + publicId +"\"}");
    }



}

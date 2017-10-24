package com.hedvig.memberservice.web;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.memberservice.externalApi.BotService;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.BankAccountDetailsList;
import com.hedvig.memberservice.web.dto.BankAccountsRetrievedEvent;
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

    public InternalMemberController(CommandBus commandBus,
                                    BillectaApi billectaApi,
                                    MemberRepository memberRepository,
                                    BotService botSerivce) {

        this.billectaApi = billectaApi;
        this.memberRepository = memberRepository;
        this.botSerivce = botSerivce;
    }


    @RequestMapping("/{memberId}/startBankAccountRetrieval")
    public ResponseEntity<String> startBankAccountRetrieval(@PathVariable Long memberId) {
        MemberEntity me = memberRepository.findOne(memberId);

        billectaApi.retrieveBankAccountNumbers(me.getSsn(), accounts -> {

            BankAccountDetailsList details = new BankAccountDetailsList(accounts);

            BankAccountsRetrievedEvent e = new BankAccountsRetrievedEvent(me.getId(), Instant.now(), details);

            botSerivce.sendEvent(e);
        });

        return ResponseEntity.noContent().build();
    }



}

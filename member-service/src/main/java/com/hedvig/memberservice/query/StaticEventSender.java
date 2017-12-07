package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.prouctsPricing.ProductApi;
import com.hedvig.memberservice.web.dto.events.MemberAuthedEvent;
import com.hedvig.memberservice.events.MemberAuthenticatedEvent;
import com.hedvig.memberservice.externalApi.BotService;
import com.hedvig.memberservice.web.dto.Member;
import com.hedvig.memberservice.web.dto.events.MemberServiceEvent;
import com.hedvig.memberservice.web.dto.events.MemberSigned;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ProcessingGroup("com.hedvig.memberservice.query")
public class StaticEventSender {

    private final BotService botService;
    private final ProductApi productApi;
    private Logger logger = LoggerFactory.getLogger(StaticEventSender.class);

    private final MemberRepository memberRepo;

    @Autowired
    public StaticEventSender(MemberRepository memberRepo, BotService botService, ProductApi productApi)
    {
        this.memberRepo = memberRepo;
        this.botService = botService;
        this.productApi = productApi;
    }

    @EventHandler
    public void on(MemberAuthenticatedEvent e, EventMessage<MemberAuthedEvent> eventMessage) {

        logger.debug("Started handling event: {}", eventMessage.getIdentifier());
        MemberEntity me = memberRepo.findOne(e.getMemberId());

        Member p = new Member(me);

        MemberAuthedEvent externalEvent = new MemberAuthedEvent(
                e.getMemberId(),
                eventMessage.getIdentifier(),
                eventMessage.getTimestamp(),
                p);

        logger.info("Sening MemberAuthenticatedEvent {}", externalEvent);

        botService.sendEvent(externalEvent);

        logger.debug("Completed handling event: {}", eventMessage.getIdentifier());
    }

    @EventHandler
    public void on(MemberSignedEvent e, EventMessage<MemberSignedEvent> eventMessage) {

        MemberSigned externalEventPayload = new MemberSigned(e.getReferenceId());
        MemberServiceEvent externalEvent = new MemberServiceEvent(e.getId(), eventMessage.getTimestamp(), externalEventPayload);
        
        productApi.contractSinged(e.getId(), e.getReferenceId());
    }
}

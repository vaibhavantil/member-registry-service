package com.hedvig.memberservice.query;

import com.hedvig.botService.web.dto.MemberAuthedEvent;
import com.hedvig.memberservice.events.MemberAuthenticatedEvent;
import com.hedvig.memberservice.externalApi.BotService;
import com.hedvig.memberservice.web.dto.Member;
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
    private Logger logger = LoggerFactory.getLogger(StaticEventSender.class);

    private RestTemplate restTemplate;
    private final MemberRepository memberRepo;

    @Autowired
    public StaticEventSender(RestTemplate template, MemberRepository memberRepo, BotService botService)
    {
        this.restTemplate = template;
        this.memberRepo = memberRepo;
        this.botService = botService;
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
}

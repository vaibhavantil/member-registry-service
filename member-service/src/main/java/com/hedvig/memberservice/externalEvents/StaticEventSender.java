package com.hedvig.memberservice.externalEvents;

import com.hedvig.botService.web.dto.MemberAuthedEvent;
import com.hedvig.memberservice.events.MemberAuthenticatedEvent;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.Member;
import com.hedvig.memberservice.web.dto.Profile;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class StaticEventSender {

    private Logger log = LoggerFactory.getLogger(StaticEventSender.class);

    @Value("${hedvig.bot-service.location:localhost:4081}")
    private String botServiceLocation;

    private RestTemplate restTemplate;
    private final MemberRepository memberRepo;

    @Autowired
    public StaticEventSender(RestTemplate template, MemberRepository memberRepo)
    {
        this.restTemplate = template;
        this.memberRepo = memberRepo;
    }

    @EventHandler
    public void on(MemberAuthenticatedEvent e, EventMessage<MemberAuthedEvent> eventMessage) {

        MemberEntity me = memberRepo.findOne(e.getMemberId());

        Member p = new Member(me);

        MemberAuthedEvent externalEvent = new MemberAuthedEvent(
                e.getMemberId(),
                eventMessage.getIdentifier(),
                eventMessage.getTimestamp(),
                p);

        String url = "http://" + botServiceLocation + "/event/memberservice";

        HttpEntity<String> response = restTemplate.postForEntity(
                url,
                externalEvent,
                String.class);

        log.debug(response.toString());
    }
}

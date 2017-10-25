package com.hedvig.memberservice.externalApi;

import com.hedvig.memberservice.web.dto.events.MemberAuthedEvent;
import com.hedvig.memberservice.web.dto.events.MemberServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BotService {

    private Logger logger = LoggerFactory.getLogger(BotService.class);

    @Value("${hedvig.bot-service.location:localhost:4081}")
    private String botServiceLocation;

    private final RestTemplate restTemplate;

    public BotService( RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEvent(MemberAuthedEvent event) {
        String url = "http://" + botServiceLocation + "/event/memberservice";
        sendEventTo(url, event);
    }

    public void sendEvent(MemberServiceEvent event) {
        String url = "http://" + botServiceLocation + "/event/memberservice/bankaccountsretreived";
        sendEventTo(url, event);
    }

    private void sendEventTo(String url, Object event) {
        HttpEntity<String> response = restTemplate.postForEntity(
                url,
                event,
                String.class);

        logger.debug(response.toString());

    }
}

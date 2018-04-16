package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("com.hedvig.memberservice.query")
public class StaticEventSender {

    private final ProductApi productApi;
    private Logger logger = LoggerFactory.getLogger(StaticEventSender.class);

    private final MemberRepository memberRepo;

    @Autowired
    public StaticEventSender(MemberRepository memberRepo, ProductApi productApi)
    {
        this.memberRepo = memberRepo;
        this.productApi = productApi;
    }

    @EventHandler
    public void on(MemberSignedEvent e, EventMessage<MemberSignedEvent> eventMessage) {
        productApi.contractSinged(e.getId(), e.getReferenceId(), e.getSignature(), e.getOscpResponse());
    }
}

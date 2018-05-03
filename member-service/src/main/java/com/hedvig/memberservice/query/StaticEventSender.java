package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.MemberCancellationEvent;
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

    @Autowired
    public StaticEventSender(MemberRepository memberRepo, ProductApi productApi)
    {
        this.productApi = productApi;
    }

    @EventHandler
    public void on(MemberSignedEvent e, EventMessage<MemberSignedEvent> eventMessage) {
        productApi.contractSinged(e.getId(), e.getReferenceId(), e.getSignature(), e.getOscpResponse());
    }

    @EventHandler
    public void on(MemberCancellationEvent e) {
        logger.info("Sending member cancellation command to product-pricing for member: ", e.getMemberId());
        try {
            productApi.memberCancelledInsurance(e.getMemberId(), e.getInactivationDate());
        }catch (RuntimeException ex) {
            logger.error("Could not cancel member at product-pricing: {}", ex.getMessage(), ex);
            //TODO Send event to sentry
        }
    }
}

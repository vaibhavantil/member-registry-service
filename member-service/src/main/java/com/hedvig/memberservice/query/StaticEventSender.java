package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.InsuranceCancellationEvent;
import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.services.SigningService;
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
  private SigningService signService;

  @Autowired
  public StaticEventSender(MemberRepository memberRepo, ProductApi productApi,
      SigningService signService) {
    this.productApi = productApi;
    this.signService = signService;
  }

  @EventHandler
  public void on(MemberSignedEvent e, EventMessage<MemberSignedEvent> eventMessage) {

    productApi.contractSinged(
        e.getId(),
        e.getReferenceId(),
        e.getSignature(),
        e.getOscpResponse(),
        eventMessage.getTimestamp(),
        e.getSsn()
        );

    signService.productSignConfirmed(e.getReferenceId());

  }

  @EventHandler
  public void on(InsuranceCancellationEvent e) {
    logger.info(
        "Sending member cancellation command to product-pricing for member: ", e.getMemberId());
    try {
      productApi.memberCancelledInsurance(
          e.getMemberId(), e.getInsuranceId(), e.getInactivationDate());
    } catch (RuntimeException ex) {
      logger.error("Could not cancel member at product-pricing: {}", ex.getMessage(), ex);
      // TODO Send event to sentry
    }
  }
}

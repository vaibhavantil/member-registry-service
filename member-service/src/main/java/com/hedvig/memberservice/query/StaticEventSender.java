package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.InsuranceCancellationEvent;
import com.hedvig.integration.productsPricing.ProductApi;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
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
  public StaticEventSender(ProductApi productApi) {
    this.productApi = productApi;
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
    }
  }
}

package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.services.SigningService;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Saga(configurationBean = "memberSignedSagaConfiguration")
public class MemberSignedSaga {

  private static Logger log = LoggerFactory.getLogger(MemberSignedSaga.class);

  @Autowired transient ProductApi productApi;

  @Autowired transient SigningService signingService;

  @SagaEventHandler(associationProperty = "id")
  @StartSaga
  @EndSaga
  public void onMemberSignedEvent(
      MemberSignedEvent e, EventMessage<MemberSignedEvent> eventMessage) {

    log.debug("ON MEMBER SIGNED EVENT FOR {}", e.getId());

    try{
      productApi.contractSinged(
          e.getId(),
          e.getReferenceId(),
          e.getSignature(),
          e.getOscpResponse(),
          eventMessage.getTimestamp(),
          e.getSsn());
    }catch (RuntimeException ex) {
      log.error("Could not notify product-pricing about signed member for memberId: {}", e.getId());
    }


    signingService.productSignConfirmed(e.getReferenceId());
  }

}

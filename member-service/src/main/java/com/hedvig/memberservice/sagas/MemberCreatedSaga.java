package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Saga(configurationBean = "memberCreatedSagaConfiguration")
public class MemberCreatedSaga {

  private static Logger log = LoggerFactory.getLogger(MemberCreatedSaga.class);

  @Autowired
  transient ProductApi productApi;

  @SagaEventHandler(associationProperty = "id")
  @StartSaga
  @EndSaga
  public void onMemberCreatedEvent(
    MemberCreatedEvent e, EventMessage<MemberCreatedEvent> eventMessage) {

    log.debug("ON MEMBER CREATED EVENT FOR {}", e.getId());

    try {
      productApi.memberCreated(e.getId());
    } catch (RuntimeException ex) {
      log.error("Could not notify product-pricing about created member for memberId: {}", e.getId(), ex);
    }
  }

}

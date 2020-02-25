package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.integration.productsPricing.ProductApi;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Saga(configurationBean = "memberCreatedSagaConfiguration")
public class MemberCreatedSaga {

  @Autowired
  transient ProductApi productApi;

  @SagaEventHandler(associationProperty = "id")
  @StartSaga
  @EndSaga
  public void onMemberCreatedEvent(MemberCreatedEvent e) {

    log.debug("ON MEMBER CREATED EVENT FOR {}", e.getId());

    try {
//      productApi.memberCreated(e.getId());
    } catch (RuntimeException ex) {
      log.error("Could not notify product-pricing about created member for memberId: {}", e.getId(), ex);
    }
  }

}

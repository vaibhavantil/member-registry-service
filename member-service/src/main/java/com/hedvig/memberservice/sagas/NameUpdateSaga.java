package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Saga(configurationBean = "memberMemberNameUpdateConfiguration")
public class NameUpdateSaga {

  @Autowired
  transient ProductApi productApi;

  @SagaEventHandler(associationProperty = "memberId")
  @StartSaga
  @EndSaga
  public void onMemberNameUpdate(NameUpdatedEvent e) {

    log.debug("ON MEMBER NAME UPDATE EVENT FOR {}", e.getMemberId());

    try {
      productApi.memberNameUpdate(e.getMemberId(), e.getFirstName());
    } catch (RuntimeException ex) {
      log.error("Could not notify product-pricing about member name update for memberId: {}", e.getMemberId(), ex);
    }
  }

}

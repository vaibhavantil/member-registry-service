package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.integration.productsPricing.ProductApi;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@RunWith(MockitoJUnitRunner.class)
public class MemberCreatedSagaTest {

  @Mock
  ProductApi productApi;

  @Test
  public void onMemberCreatedEvent_whenProductApiThrowsRuntimeException_willCallSigningService() {
    val saga = new MemberCreatedSaga();
    saga.productApi = productApi;

    final MemberCreatedEvent e = new MemberCreatedEvent(1337L, MemberStatus.INITIATED);
    saga.onMemberCreatedEvent(e);

    then(productApi).should().memberCreated(1337L);
  }
}

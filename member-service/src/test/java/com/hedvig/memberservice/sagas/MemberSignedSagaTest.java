package com.hedvig.memberservice.sagas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.services.SNSNotificationService;
import com.hedvig.memberservice.services.SigningService;
import lombok.val;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MemberSignedSagaTest {

  @Mock
  ProductApi productApi;
  @Mock
  SigningService signingService;
  @Mock
  SNSNotificationService snsNotificationService;

  @Test
  public void onMemberSignedEvent_whenProductApiThrowsRuntimeException_willCallSigningService() {

    willThrow(RuntimeException.class).given(productApi).contractSinged(anyLong(), anyString(), anyString(), anyString(), any(), anyString());

    val saga = new MemberSignedSaga();
    saga.productApi = productApi;
    saga.signingService = signingService;
    saga.snsNotificationService = snsNotificationService;

    final MemberSignedEvent e = new MemberSignedEvent(1337L, "referenceId", "signature",
        "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
        e, new GenericEventMessage<>(e));

    then(signingService).should().productSignConfirmed(e.getReferenceId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getId());
  }
}
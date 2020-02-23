package com.hedvig.memberservice.sagas;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.hedvig.integration.underwriter.UnderwriterApi;
import com.hedvig.memberservice.events.MemberSignedEvent;
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
  UnderwriterApi underwriterApi;
  @Mock
  SigningService signingService;
  @Mock
  SNSNotificationService snsNotificationService;

  @Test
  public void onMemberSignedEvent_whenProductApiThrowsRuntimeException_willCallSigningService() {

    willThrow(RuntimeException.class).given(underwriterApi).memberSigned(anyString(), anyString(), anyString(), anyString());

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);

    final MemberSignedEvent e = new MemberSignedEvent(1337L, "referenceId", "signature",
        "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
        e, new GenericEventMessage<>(e));

    then(signingService).should().swedishProductSignConfirmed(e.getReferenceId());
    then(signingService).should().productSignConfirmed(e.getId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getId());
  }
}

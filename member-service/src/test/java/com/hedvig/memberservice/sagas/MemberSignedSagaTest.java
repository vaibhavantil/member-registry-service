package com.hedvig.memberservice.sagas;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

import com.hedvig.integration.underwriter.UnderwriterApi;
import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent;
import com.hedvig.memberservice.services.SNSNotificationService;
import com.hedvig.memberservice.services.SigningService;
import com.hedvig.memberservice.services.UnderwriterSigningService;
import lombok.val;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MemberSignedSagaTest {

  @Mock
  UnderwriterApi underwriterApi;
  @Mock
  UnderwriterSigningService underwriterSigningService;
  @Mock
  SigningService signingService;
  @Mock
  SNSNotificationService snsNotificationService;

  @Test
  public void onMemberSignedEvent_whenUnderwriterApiThrowsRuntimeException_willCallSigningService() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(false);
    willThrow(RuntimeException.class).given(underwriterApi).memberSigned(anyString(), anyString(), anyString(), anyString());

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);


    final MemberSignedEvent e = new MemberSignedEvent(1337L, "123e4567-e89b-12d3-a456-426655440000", "signature",
      "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
      e, new GenericEventMessage<>(e));

    then(signingService).should().completeSwedishSession(e.getReferenceId());
    then(signingService).should().productSignConfirmed(e.getId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getId());
  }

  @Test
  public void onMemberSignedEvent_whenUnderwriterServiceThrowsRuntimeException_willCallSigningService() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(true);
    willThrow(RuntimeException.class).given(underwriterSigningService).swedishBankIdSignSessionWasCompleted(anyString(), anyString(), anyString());

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);


    final MemberSignedEvent e = new MemberSignedEvent(1337L, "123e4567-e89b-12d3-a456-426655440000", "signature",
      "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
      e, new GenericEventMessage<>(e));

    then(signingService).should().completeSwedishSession(e.getReferenceId());
    then(signingService).should().productSignConfirmed(e.getId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getId());
  }

  @Test
  public void onMemberSignedEvent_whenUnderwriterHandleSigningSession_dontCallMemberSignedEndpoint() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(true);

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    final MemberSignedEvent e = new MemberSignedEvent(1337L, "123e4567-e89b-12d3-a456-426655440000", "signature",
      "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
      e, new GenericEventMessage<>(e));

    then(underwriterSigningService).should().swedishBankIdSignSessionWasCompleted(e.getReferenceId(), e.getSignature(), e.getOscpResponse());
    verifyZeroInteractions(underwriterApi);
  }

  @Test
  public void onMemberSignedEvent_whenIsNotUnderwriterHandleSigningSession_callMemberSignedEndpoint() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(false);

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    verifyNoMoreInteractions(underwriterSigningService);

    final MemberSignedEvent e = new MemberSignedEvent(1337L, "123e4567-e89b-12d3-a456-426655440000", "signature",
      "oscpResponse", "19121212121212");
    saga.onMemberSignedEvent(
      e, new GenericEventMessage<>(e));

    then(underwriterApi).should().memberSigned(e.id.toString(), e.referenceId, e.signature, e.oscpResponse);
  }

  @Test
  public void onNorwegianMemberSignedEvent_whenUnderwriterApiThrowsRuntimeException_willCallSigningService() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(false);
    willThrow(RuntimeException.class).given(underwriterApi).memberSigned(anyString(), anyString(), anyString(), anyString());

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    final NorwegianMemberSignedEvent e = new NorwegianMemberSignedEvent(1337L, "12121212120", "{ \"json\":true }", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
    saga.onNorwegianMemberSignedEvent(
      e, new GenericEventMessage(e));

    then(signingService).should().productSignConfirmed(e.getMemberId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getMemberId());
  }

  @Test
  public void onNorwegianMemberSignedEvent_whenUnderwriterServiceThrowsRuntimeException_willCallSigningService() {
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
    when(underwriterSigningService.isUnderwriterHandlingSignSession(uuid)).thenReturn(true);
    willThrow(RuntimeException.class).given(underwriterSigningService).norwegianBankIdSignSessionWasCompleted(uuid);

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    final NorwegianMemberSignedEvent e = new NorwegianMemberSignedEvent(1337L, "12121212120", "{ \"json\":true }", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
    saga.onNorwegianMemberSignedEvent(
      e, new GenericEventMessage(e));

    then(signingService).should().productSignConfirmed(e.getMemberId());
    then(snsNotificationService).should().sendMemberSignedNotification(e.getMemberId());
  }

  @Test
  public void onNorwegianMemberSignedEvent_whenUnderwriterHandleSigningSession_dontCallMemberSignedEndpoint() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(true);

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    final NorwegianMemberSignedEvent e = new NorwegianMemberSignedEvent(1337L, "12121212120", "{ \"json\":true }", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
    saga.onNorwegianMemberSignedEvent(
      e, new GenericEventMessage(e));

    then(underwriterSigningService).should().norwegianBankIdSignSessionWasCompleted(e.getReferenceId());
    verifyZeroInteractions(underwriterApi);
  }

  @Test
  public void onNorwegianMemberSignedEvent_whenIsNotUnderwriterHandleSigningSession_callMemberSignedEndpoint() {
    when(underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))).thenReturn(false);

    val saga = new MemberSignedSaga();
    saga.setUnderwriterApi(underwriterApi);
    saga.setSigningService(signingService);
    saga.setSnsNotificationService(snsNotificationService);
    saga.setUnderwriterSigningService(underwriterSigningService);

    verifyNoMoreInteractions(underwriterSigningService);

    final NorwegianMemberSignedEvent e = new NorwegianMemberSignedEvent(1337L, "12121212120", "{ \"json\":true }", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
    saga.onNorwegianMemberSignedEvent(
      e, new GenericEventMessage(e));

    then(underwriterApi).should().memberSigned("1337", "", "", "");
  }
}
